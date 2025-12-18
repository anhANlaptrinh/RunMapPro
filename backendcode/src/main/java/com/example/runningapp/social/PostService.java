package com.example.runningapp.social;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.runningapp.common.SecurityUtils;
import com.example.runningapp.common.exception.BadRequestException;
import com.example.runningapp.common.exception.NotFoundException;
import com.example.runningapp.common.exception.UnauthorizedException;
import com.example.runningapp.group.Group;
import com.example.runningapp.group.GroupMemberRepository;
import com.example.runningapp.group.GroupRepository;
import com.example.runningapp.media.MediaService;
import com.example.runningapp.notification.NotificationService;
import com.example.runningapp.notification.NotificationType;
import com.example.runningapp.run.model.Run;
import com.example.runningapp.run.repository.RunRepository;
import com.example.runningapp.social.dto.CreateCommentRequest;
import com.example.runningapp.social.dto.CreatePostRequest;
import com.example.runningapp.social.dto.SharePostRequest;
import com.example.runningapp.user.User;
import com.example.runningapp.user.UserRepository;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final MediaService mediaService;
    private final NotificationService notificationService;
    private final RunRepository runRepository;

    public PostService(PostRepository postRepository,
            PostLikeRepository postLikeRepository,
            CommentRepository commentRepository,
            CommentLikeRepository commentLikeRepository,
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository,
            UserRepository userRepository,
            MediaService mediaService,
            NotificationService notificationService,
            RunRepository runRepository) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.commentRepository = commentRepository;
        this.commentLikeRepository = commentLikeRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.mediaService = mediaService;
        this.notificationService = notificationService;
        this.runRepository = runRepository;
    }

    public Post createPost(CreatePostRequest request) {
        String userId = requireUser();
        Instant now = Instant.now();
        String visibility = "public";
        Group group = null;
        
        // Validate runId if provided
        if (request.runId() != null && !request.runId().isEmpty()) {
            Run run = runRepository.findById(request.runId())
                    .orElseThrow(() -> new NotFoundException("Run not found"));
            // Verify run belongs to the authenticated user
            if (!run.getUserId().equals(userId)) {
                throw new UnauthorizedException("Cannot attach a run that doesn't belong to you");
            }
        }
        
        if (request.groupId() != null) {
            group = groupRepository.findByIdAndBlockedFalse(request.groupId())
                    .orElseThrow(() -> new NotFoundException("Group not found"));
            if (!groupMemberRepository.existsByGroupIdAndUserId(group.getId(), userId)) {
                throw new BadRequestException("Join the group before posting");
            }
            visibility = "group_only";
        }
        Post post = Post.builder()
                .authorId(userId)
                .contentText(request.contentText())
            .mediaIds(request.mediaIds() == null ? Collections.emptyList() : request.mediaIds())
                .groupId(request.groupId())
                .runId(request.runId())
                .visibility(visibility)
                .likeCount(0)
                .commentCount(0)
                .shareCount(0)
                .deleted(false)
                .blocked(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
        post = postRepository.save(post);
        if (group != null) {
            group.setPostCount(group.getPostCount() + 1);
            groupRepository.save(group);
        }
        return post;
    }

    public List<Post> publicFeed(int page, int size) {
        String userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> result = postRepository.findByVisibilityInAndDeletedFalseAndBlockedFalse(List.of("public"), pageable);
        List<Post> posts = result.getContent();
        
        // Enrich with author info
        enrichWithAuthorInfo(posts);
        
        // Enrich with like status if user is authenticated
        if (userId != null) {
            enrichWithLikeStatus(posts, userId);
        }
        
        return posts;
    }

    public Post getPostById(String postId) {
        String userId = SecurityUtils.getCurrentUserId();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        if (post.isDeleted() || post.isBlocked()) {
            throw new NotFoundException("Post not found");
        }
        
        // Enrich with author info
        enrichWithAuthorInfo(Collections.singletonList(post));
        
        // Enrich with like status if user is authenticated
        if (userId != null) {
            enrichWithLikeStatus(Collections.singletonList(post), userId);
        }
        
        return post;
    }

    public List<Post> userPosts(String userId, int page, int size) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> result = postRepository.findByAuthorIdAndDeletedFalseAndBlockedFalse(userId, pageable);
        List<Post> posts = result.getContent();
        
        // Enrich with author info
        enrichWithAuthorInfo(posts);
        
        // Enrich with like status if user is authenticated
        if (currentUserId != null) {
            enrichWithLikeStatus(posts, currentUserId);
        }
        
        return posts;
    }

    public Post likePost(String postId) {
        String userId = requireUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        postLikeRepository.findByPostIdAndUserId(postId, userId).ifPresent(existing -> {
            throw new BadRequestException("Already liked");
        });
        PostLike like = PostLike.builder()
                .postId(postId)
                .userId(userId)
                .createdAt(Instant.now())
                .build();
        postLikeRepository.save(like);
        post.setLikeCount(post.getLikeCount() + 1);
        post.setLikedByCurrentUser(true);
        postRepository.save(post);
        
        // Create notification
        notificationService.createNotification(
            post.getAuthorId(), userId, NotificationType.LIKE_POST, 
            postId, null, null
        );
        
        // Enrich with author info
        enrichWithAuthorInfo(List.of(post));
        
        return post;
    }

    public Post unlikePost(String postId) {
        String userId = requireUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        postLikeRepository.findByPostIdAndUserId(postId, userId).ifPresent(like -> {
            postLikeRepository.delete(like);
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        });
        post.setLikedByCurrentUser(false);
        postRepository.save(post);
        
        // Enrich with author info
        enrichWithAuthorInfo(List.of(post));
        
        return post;
    }

    public Comment addComment(String postId, CreateCommentRequest request) {
        String userId = requireUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        Comment comment = Comment.builder()
                .postId(postId)
                .authorId(userId)
                .contentText(request.contentText())
                .parentCommentId(request.parentCommentId())
                .likeCount(0)
                .deleted(false)
                .blocked(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        comment = commentRepository.save(comment);
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);
        
        // Create notification
        if (request.parentCommentId() != null) {
            // Reply notification
            Comment parentComment = commentRepository.findById(request.parentCommentId()).orElse(null);
            if (parentComment != null) {
                notificationService.createNotification(
                    parentComment.getAuthorId(), userId, NotificationType.REPLY_COMMENT,
                    postId, comment.getId(), request.contentText()
                );
            }
        } else {
            // New comment notification
            notificationService.createNotification(
                post.getAuthorId(), userId, NotificationType.COMMENT_POST,
                postId, comment.getId(), request.contentText()
            );
        }
        
        // Enrich with author info before returning
        enrichCommentsWithAuthorInfo(Collections.singletonList(comment));
        
        return comment;
    }

    public List<Comment> getComments(String postId) {
        String userId = SecurityUtils.getCurrentUserId();
        List<Comment> comments = commentRepository.findByPostIdAndDeletedFalseOrderByCreatedAtAsc(postId);
        enrichCommentsWithAuthorInfo(comments);
        
        // Enrich with like status if user is authenticated
        if (userId != null) {
            enrichCommentsWithLikeStatus(comments, userId);
        }
        
        return comments;
    }

    public List<Comment> getComments(String postId, int page, int size) {
        String userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<Comment> result = commentRepository.findByPostIdAndDeletedFalse(postId, pageable);
        List<Comment> comments = result.getContent();
        enrichCommentsWithAuthorInfo(comments);
        
        // Enrich with like status if user is authenticated
        if (userId != null) {
            enrichCommentsWithLikeStatus(comments, userId);
        }
        
        return comments;
    }

    public Post sharePost(String postId, SharePostRequest request) {
        String userId = requireUser();
        Post original = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        Instant now = Instant.now();
        String visibility = "public";
        if (request.groupId() != null) {
            Group targetGroup = groupRepository.findByIdAndBlockedFalse(request.groupId())
                .orElseThrow(() -> new NotFoundException("Group not found"));
            if (!groupMemberRepository.existsByGroupIdAndUserId(targetGroup.getId(), userId)) {
            throw new UnauthorizedException("Join the group before sharing");
            }
            visibility = "group_only";
        }
        Post shared = Post.builder()
                .authorId(userId)
                .contentText(request.contentText())
            .mediaIds(Collections.emptyList())
                .groupId(request.groupId())
                .originalPostId(original.getId())
            .visibility(visibility)
                .likeCount(0)
                .commentCount(0)
                .shareCount(0)
                .deleted(false)
                .blocked(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
        shared = postRepository.save(shared);
        original.setShareCount(original.getShareCount() + 1);
        postRepository.save(original);
        
        // Create notification
        notificationService.createNotification(
            original.getAuthorId(), userId, NotificationType.SHARE_POST,
            original.getId(), null, request.contentText()
        );
        
        return shared;
    }

    public List<Post> groupFeed(String groupId) {
        String userId = requireUser();
        Group group = groupRepository.findByIdAndBlockedFalse(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)
                && "private".equalsIgnoreCase(group.getPrivacy())) {
            throw new UnauthorizedException("Join the group to view posts");
        }
        return postRepository.findByGroupIdAndDeletedFalseAndBlockedFalseOrderByCreatedAtDesc(groupId);
    }

    private String requireUser() {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("Authentication required");
        }
        return userId;
    }

    private void enrichWithLikeStatus(List<Post> posts, String userId) {
        if (posts == null || posts.isEmpty()) {
            return;
        }
        
        List<String> postIds = posts.stream().map(Post::getId).toList();
        List<PostLike> likes = postLikeRepository.findByPostIdInAndUserId(postIds, userId);
        
        // Create a set of liked post IDs for quick lookup
        var likedPostIds = likes.stream()
            .map(PostLike::getPostId)
            .collect(java.util.stream.Collectors.toSet());
        
        // Mark posts as liked
        for (Post post : posts) {
            post.setLikedByCurrentUser(likedPostIds.contains(post.getId()));
        }
    }
    
    private void enrichWithAuthorInfo(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return;
        }
        
        // Get unique author IDs
        List<String> authorIds = posts.stream()
            .map(Post::getAuthorId)
            .distinct()
            .toList();
        
        // Fetch all authors
        List<User> authors = userRepository.findByIdIn(authorIds);
        
        // Create map for quick lookup
        Map<String, User> authorMap = authors.stream()
            .collect(Collectors.toMap(User::getId, user -> user));
        
        // Populate author info
        for (Post post : posts) {
            User author = authorMap.get(post.getAuthorId());
            if (author != null) {
                post.setAuthorName(author.getFullName() != null && !author.getFullName().isEmpty() 
                    ? author.getFullName() 
                    : author.getUsername());
                post.setAuthorAvatar(mediaService.buildPublicUrl(author.getAvatarMediaId()));
            }
            
            // Enrich original post if this is a share
            if (post.getOriginalPostId() != null) {
                postRepository.findById(post.getOriginalPostId()).ifPresent(originalPost -> {
                    // Enrich the original post with author info
                    User originalAuthor = authorMap.get(originalPost.getAuthorId());
                    if (originalAuthor == null) {
                        originalAuthor = userRepository.findById(originalPost.getAuthorId()).orElse(null);
                    }
                    if (originalAuthor != null) {
                        originalPost.setAuthorName(originalAuthor.getFullName() != null && !originalAuthor.getFullName().isEmpty() 
                            ? originalAuthor.getFullName() 
                            : originalAuthor.getUsername());
                        originalPost.setAuthorAvatar(mediaService.buildPublicUrl(originalAuthor.getAvatarMediaId()));
                    }
                    post.setOriginalPost(originalPost);
                });
            }
        }
    }
    
    private void enrichCommentsWithLikeStatus(List<Comment> comments, String userId) {
        if (comments == null || comments.isEmpty()) {
            return;
        }
        
        // Get comment IDs
        List<String> commentIds = comments.stream()
            .map(Comment::getId)
            .toList();
        
        // Fetch user's likes for these comments
        List<CommentLike> userLikes = commentLikeRepository.findByCommentIdInAndUserId(commentIds, userId);
        
        // Create set for quick lookup
        var likedCommentIds = userLikes.stream()
            .map(CommentLike::getCommentId)
            .collect(Collectors.toSet());
        
        // Set liked status
        for (Comment comment : comments) {
            comment.setLikedByCurrentUser(likedCommentIds.contains(comment.getId()));
        }
    }
    
    private void enrichCommentsWithAuthorInfo(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }
        
        // Get unique author IDs
        List<String> authorIds = comments.stream()
            .map(Comment::getAuthorId)
            .distinct()
            .toList();
        
        // Fetch all authors
        List<User> authors = userRepository.findByIdIn(authorIds);
        
        // Create map for quick lookup
        Map<String, User> authorMap = authors.stream()
            .collect(Collectors.toMap(User::getId, user -> user));
        
        // Populate author info
        for (Comment comment : comments) {
            User author = authorMap.get(comment.getAuthorId());
            if (author != null) {
                comment.setAuthorName(author.getFullName() != null && !author.getFullName().isEmpty() 
                    ? author.getFullName() 
                    : author.getUsername());
                comment.setAuthorAvatar(mediaService.buildPublicUrl(author.getAvatarMediaId()));
            }
        }
    }
    
    public Comment likeComment(String commentId) {
        String userId = requireUser();
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new NotFoundException("Comment not found"));
        
        // Check if already liked
        if (commentLikeRepository.findByCommentIdAndUserId(commentId, userId).isPresent()) {
            throw new BadRequestException("Already liked this comment");
        }
        
        // Create like
        CommentLike like = CommentLike.builder()
            .commentId(commentId)
            .userId(userId)
            .createdAt(Instant.now())
            .build();
        commentLikeRepository.save(like);
        
        // Update like count
        comment.setLikeCount(comment.getLikeCount() + 1);
        comment = commentRepository.save(comment);
        
        // Create notification
        notificationService.createNotification(
            comment.getAuthorId(), userId, NotificationType.LIKE_COMMENT,
            comment.getPostId(), commentId, null
        );
        
        // Enrich with author info and like status
        enrichCommentsWithAuthorInfo(List.of(comment));
        comment.setLikedByCurrentUser(true);
        
        return comment;
    }
    
    public Comment unlikeComment(String commentId) {
        String userId = requireUser();
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new NotFoundException("Comment not found"));
        
        // Check if liked
        CommentLike like = commentLikeRepository.findByCommentIdAndUserId(commentId, userId)
            .orElseThrow(() -> new BadRequestException("You haven't liked this comment"));
        
        // Delete like
        commentLikeRepository.delete(like);
        
        // Update like count
        comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
        comment = commentRepository.save(comment);
        
        // Enrich with author info and like status
        enrichCommentsWithAuthorInfo(List.of(comment));
        comment.setLikedByCurrentUser(false);
        
        return comment;
    }
    
    public Post updatePost(String postId, CreatePostRequest request) {
        String userId = requireUser();
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new NotFoundException("Post not found"));
        
        // Check if user is the author
        if (!post.getAuthorId().equals(userId)) {
            throw new UnauthorizedException("You can only update your own posts");
        }
        
        // Validate runId if provided
        if (request.runId() != null && !request.runId().isEmpty()) {
            Run run = runRepository.findById(request.runId())
                    .orElseThrow(() -> new NotFoundException("Run not found"));
            // Verify run belongs to the authenticated user
            if (!run.getUserId().equals(userId)) {
                throw new UnauthorizedException("Cannot attach a run that doesn't belong to you");
            }
        }
        
        // Update fields
        post.setContentText(request.contentText());
        if (request.mediaIds() != null) {
            post.setMediaIds(request.mediaIds());
        }
        post.setRunId(request.runId());
        post.setUpdatedAt(Instant.now());
        
        post = postRepository.save(post);
        
        // Enrich with author info and like status
        enrichWithAuthorInfo(List.of(post));
        if (userId != null) {
            enrichWithLikeStatus(List.of(post), userId);
        }
        
        return post;
    }
    
    public void deletePost(String postId) {
        String userId = requireUser();
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new NotFoundException("Post not found"));
        
        // Check if user is the author
        if (!post.getAuthorId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own posts");
        }
        
        // Soft delete
        post.setDeleted(true);
        post.setUpdatedAt(Instant.now());
        postRepository.save(post);
    }
    
    public Comment updateComment(String commentId, CreateCommentRequest request) {
        String userId = requireUser();
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new NotFoundException("Comment not found"));
        
        // Check if user is the author
        if (!comment.getAuthorId().equals(userId)) {
            throw new UnauthorizedException("You can only update your own comments");
        }
        
        // Update fields
        comment.setContentText(request.contentText());
        comment.setUpdatedAt(Instant.now());
        
        comment = commentRepository.save(comment);
        
        // Enrich with author info and like status
        enrichCommentsWithAuthorInfo(List.of(comment));
        if (userId != null) {
            enrichCommentsWithLikeStatus(List.of(comment), userId);
        }
        
        return comment;
    }
    
    public void deleteComment(String commentId) {
        String userId = requireUser();
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new NotFoundException("Comment not found"));
        
        // Check if user is the author
        if (!comment.getAuthorId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own comments");
        }
        
        // Soft delete
        comment.setDeleted(true);
        comment.setUpdatedAt(Instant.now());
        commentRepository.save(comment);
        
        // Update post comment count
        Post post = postRepository.findById(comment.getPostId()).orElse(null);
        if (post != null) {
            post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
            postRepository.save(post);
        }
    }
    
    /**
     * Get the run associated with a post
     * @param postId The post ID
     * @return The Run entity if the post has an attached run
     * @throws NotFoundException if post not found or post has no run attached
     */
    public Run getRunForPost(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        
        if (post.getRunId() == null || post.getRunId().isEmpty()) {
            throw new NotFoundException("This post has no attached run");
        }
        
        return runRepository.findById(post.getRunId())
                .orElseThrow(() -> new NotFoundException("Run not found"));
    }
}

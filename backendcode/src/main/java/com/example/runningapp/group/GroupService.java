package com.example.runningapp.group;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.runningapp.common.SecurityUtils;
import com.example.runningapp.common.exception.BadRequestException;
import com.example.runningapp.common.exception.NotFoundException;
import com.example.runningapp.common.exception.UnauthorizedException;
import com.example.runningapp.group.dto.CreateGroupRequest;
import com.example.runningapp.group.dto.UpdateGroupSettingsRequest;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupJoinRequestRepository groupJoinRequestRepository;
    private final GroupPostRepository groupPostRepository;
    private final com.example.runningapp.user.UserRepository userRepository;
    private final com.example.runningapp.social.CommentRepository commentRepository;

    public GroupService(GroupRepository groupRepository, 
                       GroupMemberRepository groupMemberRepository,
                       GroupJoinRequestRepository groupJoinRequestRepository,
                       GroupPostRepository groupPostRepository,
                       com.example.runningapp.user.UserRepository userRepository,
                       com.example.runningapp.social.CommentRepository commentRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupJoinRequestRepository = groupJoinRequestRepository;
        this.groupPostRepository = groupPostRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    public Group createGroup(CreateGroupRequest request) {
        String userId = requireUser();
        Instant now = Instant.now();
        
        // Generate invite code for private groups
        String inviteCode = "private".equals(request.privacy()) ? 
            generateInviteCode() : null;
        
        // Set expiration to 3 months from now for private groups
        Instant inviteCodeExpiresAt = "private".equals(request.privacy()) ?
            now.plus(90, java.time.temporal.ChronoUnit.DAYS) : null;
        
        Group group = Group.builder()
                .name(request.name())
                .description(request.description())
                .coverImageUrl(request.coverImageUrl())
                .ownerId(userId)
                .privacy(request.privacy())
                .inviteCode(inviteCode)
                .inviteCodeExpiresAt(inviteCodeExpiresAt)
                .requireMemberApproval(false)
                .requirePostApproval(false)
                .memberCount(1)
                .postCount(0)
                .blocked(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
        group = groupRepository.save(group);
        GroupMember owner = GroupMember.builder()
                .groupId(group.getId())
                .userId(userId)
                .role("owner")
                .joinedAt(now)
                .build();
        groupMemberRepository.save(owner);
        return group;
    }

    public void joinGroup(String groupId) {
        String userId = requireUser();
        Group group = groupRepository.findByIdAndBlockedFalse(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            return;
        }
        
        // For private groups with member approval, create join request
        if ("private".equals(group.getPrivacy()) && group.isRequireMemberApproval()) {
            throw new BadRequestException("Please use requestJoinGroup for private groups with member approval");
        }
        
        GroupMember member = GroupMember.builder()
                .groupId(groupId)
                .userId(userId)
                .role("member")
                .joinedAt(Instant.now())
                .build();
        groupMemberRepository.save(member);
        group.setMemberCount(group.getMemberCount() + 1);
        groupRepository.save(group);
    }
    
    public GroupJoinRequest requestJoinGroup(String groupId, String inviteCode) {
        String userId = requireUser();
        Group group = groupRepository.findByIdAndBlockedFalse(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
                
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new BadRequestException("You are already a member");
        }
        
        if (groupJoinRequestRepository.existsByGroupIdAndUserIdAndStatus(groupId, userId, "pending")) {
            throw new BadRequestException("You already have a pending request");
        }
        
        // Verify invite code for private groups
        if ("private".equals(group.getPrivacy())) {
            if (inviteCode == null || !inviteCode.equals(group.getInviteCode())) {
                throw new BadRequestException("Invalid invite code");
            }
        }
        
        // If no approval required, join directly
        if (!group.isRequireMemberApproval()) {
            joinGroup(groupId);
            return null;
        }
        
        // Create join request for approval
        GroupJoinRequest request = GroupJoinRequest.builder()
                .groupId(groupId)
                .userId(userId)
                .inviteCode(inviteCode)
                .status("pending")
                .requestedAt(Instant.now())
                .build();
        return groupJoinRequestRepository.save(request);
    }
    
    public GroupJoinRequest joinByInviteCode(String inviteCode) {
        String userId = requireUser();
        
        // Find group by invite code
        Group group = groupRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new BadRequestException("Invalid invite code"));
        
        // Check if invite code has expired
        if (group.getInviteCodeExpiresAt() != null && 
            group.getInviteCodeExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Invite code has expired. Please request a new code from the group admin.");
        }
        
        if (groupMemberRepository.existsByGroupIdAndUserId(group.getId(), userId)) {
            throw new BadRequestException("You are already a member");
        }
        
        if (groupJoinRequestRepository.existsByGroupIdAndUserIdAndStatus(group.getId(), userId, "pending")) {
            throw new BadRequestException("You already have a pending request");
        }
        
        // If no approval required, join directly
        if (!group.isRequireMemberApproval()) {
            joinGroup(group.getId());
            return null;
        }
        
        // Create join request for approval
        GroupJoinRequest request = GroupJoinRequest.builder()
                .groupId(group.getId())
                .userId(userId)
                .inviteCode(inviteCode)
                .status("pending")
                .requestedAt(Instant.now())
                .build();
        return groupJoinRequestRepository.save(request);
    }

    public void leaveGroup(String groupId) {
        String userId = requireUser();
        Group group = groupRepository.findByIdAndBlockedFalse(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new BadRequestException("You are not part of this group"));
        
        // Check if owner is the last member
        if ("owner".equals(member.getRole()) && group.getMemberCount() <= 1) {
            // Delete the group if owner is the last member
            deleteGroup(groupId);
            return;
        }
        
        if ("owner".equals(member.getRole())) {
            throw new UnauthorizedException("Owner cannot leave the group. Transfer ownership or delete the group first.");
        }
        
        groupMemberRepository.delete(member);
        group.setMemberCount(Math.max(0, group.getMemberCount() - 1));
        groupRepository.save(group);
    }

    public List<Group> getMyGroups(int page, int size) {
        String userId = requireUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        // Get all group IDs where user is a member
        List<String> groupIds = groupMemberRepository.findByUserId(userId).stream()
                .map(GroupMember::getGroupId)
                .collect(Collectors.toList());
        
        if (groupIds.isEmpty()) {
            return List.of();
        }
        
        List<Group> groups = groupRepository.findByIdInAndBlockedFalse(groupIds, pageable);
        
        // Populate user role for each group
        groups.forEach(group -> {
            groupMemberRepository.findByGroupIdAndUserId(group.getId(), userId)
                    .ifPresent(member -> group.setUserRole(member.getRole()));
        });
        
        return groups;
    }
    
    public List<Group> getPublicGroups(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return groupRepository.findByPrivacyAndBlockedFalse("PUBLIC", pageable);
    }

    public Group getGroup(String groupId) {
        Group group = groupRepository.findByIdAndBlockedFalse(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        
        // Recalculate post count from actual approved posts
        long actualPostCount = groupPostRepository.countByGroupIdAndStatus(groupId, "approved");
        group.setPostCount((int) actualPostCount);
        
        // Populate user role if user is authenticated
        try {
            String userId = requireUser();
            groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                    .ifPresent(member -> group.setUserRole(member.getRole()));
        } catch (Exception e) {
            // User not authenticated or not a member, userRole will be null
        }
        
        return group;
    }
    
    // Admin functions
    
    @Transactional
    public Group updateGroupSettings(String groupId, UpdateGroupSettingsRequest request) {
        String userId = requireUser();
        Group group = groupRepository.findByIdAndBlockedFalse(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        
        // Check if user is owner or admin
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));
        
        if (!"owner".equals(member.getRole()) && !"admin".equals(member.getRole())) {
            throw new UnauthorizedException("Only admins can update group settings");
        }
        
        if (request.name() != null) {
            group.setName(request.name());
        }
        if (request.description() != null) {
            group.setDescription(request.description());
        }
        if (request.coverImageUrl() != null) {
            group.setCoverImageUrl(request.coverImageUrl());
        }
        if (request.requireMemberApproval() != null) {
            group.setRequireMemberApproval(request.requireMemberApproval());
        }
        if (request.requirePostApproval() != null) {
            group.setRequirePostApproval(request.requirePostApproval());
        }
        
        // Regenerate invite code if requested
        if (request.regenerateInviteCode() != null && request.regenerateInviteCode() 
                && "private".equalsIgnoreCase(group.getPrivacy())) {
            group.setInviteCode(generateInviteCode());
            // Reset expiration to 3 months from now when regenerating
            group.setInviteCodeExpiresAt(Instant.now().plus(90, java.time.temporal.ChronoUnit.DAYS));
        }        group.setUpdatedAt(Instant.now());
        return groupRepository.save(group);
    }
    
    @Transactional
    public void updateMemberRole(String groupId, String targetUserId, String newRole) {
        String userId = requireUser();
        Group group = groupRepository.findByIdAndBlockedFalse(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        
        // Only owner can change roles
        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));
        
        if (!"owner".equals(requester.getRole())) {
            throw new UnauthorizedException("Only the owner can change member roles");
        }
        
        GroupMember targetMember = groupMemberRepository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new NotFoundException("Member not found"));
        
        if ("owner".equals(targetMember.getRole())) {
            throw new BadRequestException("Cannot change owner's role");
        }
        
        if (!List.of("admin", "member").contains(newRole)) {
            throw new BadRequestException("Invalid role. Use 'admin' or 'member'");
        }
        
        targetMember.setRole(newRole);
        groupMemberRepository.save(targetMember);
    }
    
    @Transactional
    public void removeMember(String groupId, String targetUserId) {
        String userId = requireUser();
        Group group = groupRepository.findByIdAndBlockedFalse(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        
        // Owner and admins can remove members
        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));
        
        if (!"owner".equals(requester.getRole()) && !"admin".equals(requester.getRole())) {
            throw new UnauthorizedException("Only admins can remove members");
        }
        
        GroupMember targetMember = groupMemberRepository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new NotFoundException("Member not found"));
        
        if ("owner".equals(targetMember.getRole())) {
            throw new BadRequestException("Cannot remove owner");
        }
        
        groupMemberRepository.delete(targetMember);
        group.setMemberCount(Math.max(0, group.getMemberCount() - 1));
        groupRepository.save(group);
    }
    
    public List<GroupJoinRequest> getPendingJoinRequests(String groupId, int page, int size) {
        String userId = requireUser();
        
        // Check if user is admin
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));
        
        if (!"owner".equals(member.getRole()) && !"admin".equals(member.getRole())) {
            throw new UnauthorizedException("Only admins can view join requests");
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("requestedAt").descending());
        return groupJoinRequestRepository.findByGroupIdAndStatus(groupId, "pending", pageable);
    }
    
    @Transactional
    public void approveJoinRequest(String requestId) {
        String userId = requireUser();
        GroupJoinRequest request = groupJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Join request not found"));
        
        // Check if user is admin
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(request.getGroupId(), userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));
        
        if (!"owner".equals(member.getRole()) && !"admin".equals(member.getRole())) {
            throw new UnauthorizedException("Only admins can approve join requests");
        }
        
        if (!"pending".equals(request.getStatus())) {
            throw new BadRequestException("Request already processed");
        }
        
        // Add user to group
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new NotFoundException("Group not found"));
        
        GroupMember newMember = GroupMember.builder()
                .groupId(request.getGroupId())
                .userId(request.getUserId())
                .role("member")
                .joinedAt(Instant.now())
                .build();
        groupMemberRepository.save(newMember);
        
        group.setMemberCount(group.getMemberCount() + 1);
        groupRepository.save(group);
        
        // Update request
        request.setStatus("approved");
        request.setReviewedAt(Instant.now());
        request.setReviewedBy(userId);
        groupJoinRequestRepository.save(request);
    }
    
    @Transactional
    public void rejectJoinRequest(String requestId) {
        String userId = requireUser();
        GroupJoinRequest request = groupJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Join request not found"));
        
        // Check if user is admin
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(request.getGroupId(), userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));
        
        if (!"owner".equals(member.getRole()) && !"admin".equals(member.getRole())) {
            throw new UnauthorizedException("Only admins can reject join requests");
        }
        
        if (!"pending".equals(request.getStatus())) {
            throw new BadRequestException("Request already processed");
        }
        
        request.setStatus("rejected");
        request.setReviewedAt(Instant.now());
        request.setReviewedBy(userId);
        groupJoinRequestRepository.save(request);
    }
    
    // Group Posts
    
    @Transactional
    public GroupPost createGroupPost(String groupId, String content, List<String> mediaUrls, String runId) {
        String userId = requireUser();
        Group group = groupRepository.findByIdAndBlockedFalse(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        
        // Check if user is member
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new UnauthorizedException("You must be a member to post");
        }
        
        String status = group.isRequirePostApproval() ? "pending" : "approved";
        
        GroupPost post = GroupPost.builder()
                .groupId(groupId)
                .userId(userId)
                .content(content)
                .mediaUrls(mediaUrls)
                .runId(runId)
                .status(status)
                .createdAt(Instant.now())
                .likeCount(0)
                .commentCount(0)
                .build();
        
        post = groupPostRepository.save(post);
        
        // Update post count only if approved
        if ("approved".equals(status)) {
            group.setPostCount(group.getPostCount() + 1);
            groupRepository.save(group);
        }
        
        return post;
    }
    
    public List<GroupPost> getGroupPosts(String groupId, int page, int size) {
        String userId = requireUser();
        
        // Get group info to check privacy
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        
        // Check if user is member
        boolean isMember = groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);
        
        // Private groups: only members can view posts
        if ("PRIVATE".equals(group.getPrivacy()) && !isMember) {
            throw new UnauthorizedException("You must be a member to view posts in this private group");
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        // If user is member, check their role
        if (isMember) {
            GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                    .orElseThrow(() -> new NotFoundException("Member not found"));
            
            // Admins/owners can see all posts (including pending)
            if ("owner".equals(member.getRole()) || "admin".equals(member.getRole())) {
                List<GroupPost> allPosts = groupPostRepository.findByGroupId(groupId, pageable);
                populateAuthorInfo(allPosts);
                setLikedByCurrentUser(allPosts, userId);
                return allPosts;
            }
        }
        
        // Non-members or regular members: only see approved posts
        List<GroupPost> posts = groupPostRepository.findByGroupIdAndStatus(groupId, "approved", pageable);
        populateAuthorInfo(posts);
        setLikedByCurrentUser(posts, userId);
        return posts;
    }
    
    private void populateAuthorInfo(List<GroupPost> posts) {
        for (GroupPost post : posts) {
            userRepository.findById(post.getUserId()).ifPresent(user -> {
                // Use fullName if available, fallback to username
                post.setAuthorName(user.getFullName() != null && !user.getFullName().isEmpty() 
                    ? user.getFullName() 
                    : user.getUsername());
                post.setAuthorAvatar(user.getAvatarMediaId()); // Will be media ID, frontend can convert to URL
            });
        }
    }
    
    private void setLikedByCurrentUser(List<GroupPost> posts, String userId) {
        for (GroupPost post : posts) {
            if (post.getLikedByUsers() != null && post.getLikedByUsers().contains(userId)) {
                post.setLikedByCurrentUser(true);
            } else {
                post.setLikedByCurrentUser(false);
            }
        }
    }
    
    public List<GroupPost> getPendingPosts(String groupId, int page, int size) {
        String userId = requireUser();
        
        // Check if user is admin
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));
        
        if (!"owner".equals(member.getRole()) && !"admin".equals(member.getRole())) {
            throw new UnauthorizedException("Only admins can view pending posts");
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<GroupPost> posts = groupPostRepository.findByGroupIdAndStatus(groupId, "pending", pageable);
        populateAuthorInfo(posts);
        return posts;
    }
    
    @Transactional
    public void approvePost(String postId) {
        String userId = requireUser();
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        
        // Check if user is admin
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(post.getGroupId(), userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));
        
        if (!"owner".equals(member.getRole()) && !"admin".equals(member.getRole())) {
            throw new UnauthorizedException("Only admins can approve posts");
        }
        
        if (!"pending".equals(post.getStatus())) {
            throw new BadRequestException("Post already processed");
        }
        
        post.setStatus("approved");
        post.setApprovedAt(Instant.now());
        post.setApprovedBy(userId);
        groupPostRepository.save(post);
        
        // Update group post count
        Group group = groupRepository.findById(post.getGroupId())
                .orElseThrow(() -> new NotFoundException("Group not found"));
        group.setPostCount(group.getPostCount() + 1);
        groupRepository.save(group);
    }
    
    @Transactional
    public void rejectPost(String postId) {
        String userId = requireUser();
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        
        // Check if user is admin
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(post.getGroupId(), userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));
        
        if (!"owner".equals(member.getRole()) && !"admin".equals(member.getRole())) {
            throw new UnauthorizedException("Only admins can reject posts");
        }
        
        if (!"pending".equals(post.getStatus())) {
            throw new BadRequestException("Post already processed");
        }
        
        post.setStatus("rejected");
        groupPostRepository.save(post);
    }
    
    public List<GroupMember> getGroupMembers(String groupId, int page, int size) {
        String userId = requireUser();
        
        // Check if user is member
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new UnauthorizedException("You must be a member to view members");
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("joinedAt").descending());
        return groupMemberRepository.findByGroupId(groupId, pageable);
    }
    
    @Transactional
    public void deleteGroup(String groupId) {
        String userId = requireUser();
        Group group = groupRepository.findByIdAndBlockedFalse(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        
        // Check if user is owner
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));
        
        if (!"owner".equals(member.getRole())) {
            throw new UnauthorizedException("Only owner can delete the group");
        }
        
        // Delete all related data
        groupMemberRepository.deleteByGroupId(groupId);
        groupJoinRequestRepository.deleteByGroupId(groupId);
        groupPostRepository.deleteByGroupId(groupId);
        groupRepository.delete(group);
    }
    
    private String generateInviteCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String requireUser() {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("Authentication required");
        }
        return userId;
    }
    
    // Get single group post by ID
    public GroupPost getGroupPostById(String postId) {
        String userId = requireUser();
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Group post not found"));

        Group group = groupRepository.findById(post.getGroupId())
                .orElseThrow(() -> new NotFoundException("Group not found"));

        // PRIVATE groups: require membership
        // PUBLIC groups: require authentication but NOT membership
        if ("PRIVATE".equals(group.getPrivacy())) {
            if (!groupMemberRepository.existsByGroupIdAndUserId(post.getGroupId(), userId)) {
                throw new UnauthorizedException("You must be a member to view this post");
            }
        }

        // Populate author info and liked status
        populateAuthorInfo(List.of(post));
        setLikedByCurrentUser(List.of(post), userId);
        return post;
    }
    
    // Get comments for group post
    public List<com.example.runningapp.social.Comment> getGroupPostComments(String postId, int page, int size) {
        String userId = requireUser();
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Group post not found"));

        Group group = groupRepository.findById(post.getGroupId())
                .orElseThrow(() -> new NotFoundException("Group not found"));

        // PRIVATE groups: require membership
        // PUBLIC groups: require authentication but NOT membership
        if ("PRIVATE".equals(group.getPrivacy())) {
            if (!groupMemberRepository.existsByGroupIdAndUserId(post.getGroupId(), userId)) {
                throw new UnauthorizedException("You must be a member to view comments");
            }
        }

        // Reuse Comment model - postId field will store groupPostId
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        List<com.example.runningapp.social.Comment> comments =
            commentRepository.findByPostIdAndDeletedFalse(postId, pageable).getContent();

        // Populate author info for comments
        populateCommentAuthorInfo(comments);
        return comments;
    }
    
    // Add comment to group post
    public com.example.runningapp.social.Comment addGroupPostComment(String postId, String content, String parentCommentId) {
        String userId = requireUser();
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Group post not found"));
        
        // Check if user is member (only required for PRIVATE groups)
        Group group = groupRepository.findById(post.getGroupId())
                .orElseThrow(() -> new NotFoundException("Group not found"));
        
        if ("PRIVATE".equals(group.getPrivacy())) {
            if (!groupMemberRepository.existsByGroupIdAndUserId(post.getGroupId(), userId)) {
                throw new UnauthorizedException("You must be a member to comment");
            }
        }
        
        Instant now = Instant.now();
        com.example.runningapp.social.Comment comment = com.example.runningapp.social.Comment.builder()
                .postId(postId) // Store groupPostId in postId field
                .authorId(userId)
                .contentText(content)
                .parentCommentId(parentCommentId)
                .likeCount(0)
                .deleted(false)
                .blocked(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        comment = commentRepository.save(comment);
        
        // Update comment count
        post.setCommentCount(post.getCommentCount() + 1);
        groupPostRepository.save(post);
        
        // Populate author info
        populateCommentAuthorInfo(List.of(comment));
        return comment;
    }
    
    // Like group post
    public GroupPost likeGroupPost(String postId) {
        String userId = requireUser();
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Group post not found"));
        
        // Check if user is member (only required for PRIVATE groups)
        Group group = groupRepository.findById(post.getGroupId())
                .orElseThrow(() -> new NotFoundException("Group not found"));
        
        if ("PRIVATE".equals(group.getPrivacy())) {
            if (!groupMemberRepository.existsByGroupIdAndUserId(post.getGroupId(), userId)) {
                throw new UnauthorizedException("You must be a member to like posts");
            }
        }
        
        // Initialize likedByUsers list if null
        if (post.getLikedByUsers() == null) {
            post.setLikedByUsers(new java.util.ArrayList<>());
        }
        
        // Check if user already liked
        if (post.getLikedByUsers().contains(userId)) {
            throw new IllegalStateException("You already liked this post");
        }
        
        // Add user to liked list and increment count
        post.getLikedByUsers().add(userId);
        post.setLikeCount(post.getLikedByUsers().size());
        groupPostRepository.save(post);
        
        populateAuthorInfo(List.of(post));
        post.setLikedByCurrentUser(true);
        return post;
    }
    
    // Unlike group post
    public GroupPost unlikeGroupPost(String postId) {
        String userId = requireUser();
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Group post not found"));
        
        // Check if user is member (only required for PRIVATE groups)
        Group group = groupRepository.findById(post.getGroupId())
                .orElseThrow(() -> new NotFoundException("Group not found"));
        
        if ("PRIVATE".equals(group.getPrivacy())) {
            if (!groupMemberRepository.existsByGroupIdAndUserId(post.getGroupId(), userId)) {
                throw new UnauthorizedException("You must be a member to unlike posts");
            }
        }
        
        // Initialize likedByUsers list if null
        if (post.getLikedByUsers() == null) {
            post.setLikedByUsers(new java.util.ArrayList<>());
        }
        
        // Check if user has liked
        if (!post.getLikedByUsers().contains(userId)) {
            throw new IllegalStateException("You haven't liked this post");
        }
        
        // Remove user from liked list and update count
        post.getLikedByUsers().remove(userId);
        post.setLikeCount(post.getLikedByUsers().size());
        groupPostRepository.save(post);
        
        populateAuthorInfo(List.of(post));
        post.setLikedByCurrentUser(false);
        return post;
    }
    
    // Delete group post
    @Transactional
    public void deleteGroupPost(String postId) {
        String userId = requireUser();
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Group post not found"));
        
        // Check if user is author or admin/owner
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(post.getGroupId(), userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));
        
        boolean isAuthor = post.getUserId().equals(userId);
        boolean isAdminOrOwner = "admin".equals(member.getRole()) || "owner".equals(member.getRole());
        
        if (!isAuthor && !isAdminOrOwner) {
            throw new UnauthorizedException("You can only delete your own posts");
        }
        
        // Delete comments (find and delete manually since deleteByPostId doesn't exist)
        List<com.example.runningapp.social.Comment> comments = 
            commentRepository.findByPostIdAndDeletedFalseOrderByCreatedAtAsc(postId);
        commentRepository.deleteAll(comments);
        
        // Delete post
        groupPostRepository.delete(post);
        
        // Update group post count (only if post was approved)
        if ("approved".equals(post.getStatus())) {
            Group group = groupRepository.findById(post.getGroupId()).orElse(null);
            if (group != null && group.getPostCount() > 0) {
                group.setPostCount(group.getPostCount() - 1);
                groupRepository.save(group);
            }
        }
    }
    

    
    // Helper to populate comment author info
    private void populateCommentAuthorInfo(List<com.example.runningapp.social.Comment> comments) {
        if (comments.isEmpty()) return;
        
        var userIds = comments.stream()
                .map(com.example.runningapp.social.Comment::getAuthorId)
                .distinct()
                .collect(Collectors.toList());
        
        var users = userRepository.findAllById(userIds);
        var userMap = users.stream()
                .collect(Collectors.toMap(
                    com.example.runningapp.user.User::getId,
                    u -> u
                ));
        
        for (var comment : comments) {
            var user = userMap.get(comment.getAuthorId());
            if (user != null) {
                comment.setAuthorName(user.getFullName());
                comment.setAuthorAvatar(user.getAvatarMediaId());
            }
        }
    }
}

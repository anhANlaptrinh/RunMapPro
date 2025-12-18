package com.example.runningapp.group;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.runningapp.group.dto.CreateGroupPostRequest;
import com.example.runningapp.group.dto.CreateGroupRequest;
import com.example.runningapp.group.dto.JoinGroupRequest;
import com.example.runningapp.group.dto.UpdateGroupSettingsRequest;
import com.example.runningapp.social.Post;
import com.example.runningapp.social.PostService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/groups")
@Validated
public class GroupController {

    private final GroupService groupService;
    private final PostService postService;

    public GroupController(GroupService groupService, PostService postService) {
        this.groupService = groupService;
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<Group> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        return ResponseEntity.ok(groupService.createGroup(request));
    }

    @PostMapping("/{groupId}/join")
    public ResponseEntity<Map<String, String>> join(@PathVariable String groupId) {
        groupService.joinGroup(groupId);
        return ResponseEntity.ok(Map.of("status", "joined"));
    }

    @PostMapping("/{groupId}/leave")
    public ResponseEntity<Map<String, String>> leave(@PathVariable String groupId) {
        groupService.leaveGroup(groupId);
        return ResponseEntity.ok(Map.of("status", "left"));
    }

    @GetMapping("/my-groups")
    public ResponseEntity<List<Group>> getMyGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(groupService.getMyGroups(page, size));
    }
    
    @GetMapping("/public")
    public ResponseEntity<List<Group>> getPublicGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(groupService.getPublicGroups(page, size));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<Group> getGroup(@PathVariable String groupId) {
        return ResponseEntity.ok(groupService.getGroup(groupId));
    }

    @GetMapping("/{groupId}/posts")
    public ResponseEntity<List<Post>> groupFeed(@PathVariable String groupId) {
        return ResponseEntity.ok(postService.groupFeed(groupId));
    }
    
    // Join with invite code (for private groups)
    @PostMapping("/{groupId}/request-join")
    public ResponseEntity<?> requestJoin(
            @PathVariable String groupId,
            @RequestBody JoinGroupRequest request) {
        GroupJoinRequest joinRequest = groupService.requestJoinGroup(groupId, request.inviteCode());
        if (joinRequest == null) {
            return ResponseEntity.ok(Map.of("status", "joined"));
        }
        return ResponseEntity.ok(Map.of("status", "pending", "requestId", joinRequest.getId()));
    }
    
    // Join with invite code only (finds group by code)
    @PostMapping("/join-by-code")
    public ResponseEntity<?> joinByInviteCode(@RequestBody JoinGroupRequest request) {
        GroupJoinRequest joinRequest = groupService.joinByInviteCode(request.inviteCode());
        if (joinRequest == null) {
            return ResponseEntity.ok(Map.of("status", "joined"));
        }
        return ResponseEntity.ok(Map.of(
            "status", joinRequest.getStatus(),
            "requestId", joinRequest.getId(),
            "groupId", joinRequest.getGroupId()
        ));
    }
    
    // Admin: Update group settings
    @PutMapping("/{groupId}/settings")
    public ResponseEntity<Group> updateSettings(
            @PathVariable String groupId,
            @RequestBody UpdateGroupSettingsRequest request) {
        return ResponseEntity.ok(groupService.updateGroupSettings(groupId, request));
    }
    
    // Admin: Update member role
    @PutMapping("/{groupId}/members/{userId}/role")
    public ResponseEntity<Map<String, String>> updateMemberRole(
            @PathVariable String groupId,
            @PathVariable String userId,
            @RequestBody Map<String, String> body) {
        groupService.updateMemberRole(groupId, userId, body.get("role"));
        return ResponseEntity.ok(Map.of("status", "updated"));
    }
    
    // Admin: Remove member
    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Map<String, String>> removeMember(
            @PathVariable String groupId,
            @PathVariable String userId) {
        groupService.removeMember(groupId, userId);
        return ResponseEntity.ok(Map.of("status", "removed"));
    }
    
    // Admin: Get pending join requests
    @GetMapping("/{groupId}/join-requests")
    public ResponseEntity<List<GroupJoinRequest>> getPendingJoinRequests(
            @PathVariable String groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(groupService.getPendingJoinRequests(groupId, page, size));
    }
    
    // Admin: Approve join request
    @PostMapping("/join-requests/{requestId}/approve")
    public ResponseEntity<Map<String, String>> approveJoinRequest(@PathVariable String requestId) {
        groupService.approveJoinRequest(requestId);
        return ResponseEntity.ok(Map.of("status", "approved"));
    }
    
    // Admin: Reject join request
    @PostMapping("/join-requests/{requestId}/reject")
    public ResponseEntity<Map<String, String>> rejectJoinRequest(@PathVariable String requestId) {
        groupService.rejectJoinRequest(requestId);
        return ResponseEntity.ok(Map.of("status", "rejected"));
    }
    
    // Group Posts
    @PostMapping("/{groupId}/group-posts")
    public ResponseEntity<GroupPost> createPost(
            @PathVariable String groupId,
            @RequestBody CreateGroupPostRequest request) {
        return ResponseEntity.ok(groupService.createGroupPost(groupId, request.content(), request.mediaUrls(), request.runId()));
    }
    
    @GetMapping("/{groupId}/group-posts")
    public ResponseEntity<List<GroupPost>> getGroupPosts(
            @PathVariable String groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(groupService.getGroupPosts(groupId, page, size));
    }
    
    // Admin: Get pending posts
    @GetMapping("/{groupId}/group-posts/pending")
    public ResponseEntity<List<GroupPost>> getPendingPosts(
            @PathVariable String groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(groupService.getPendingPosts(groupId, page, size));
    }
    
    // Admin: Approve post
    @PostMapping("/group-posts/{postId}/approve")
    public ResponseEntity<Map<String, String>> approvePost(@PathVariable String postId) {
        groupService.approvePost(postId);
        return ResponseEntity.ok(Map.of("status", "approved"));
    }
    
    // Admin: Reject post
    @PostMapping("/group-posts/{postId}/reject")
    public ResponseEntity<Map<String, String>> rejectPost(@PathVariable String postId) {
        groupService.rejectPost(postId);
        return ResponseEntity.ok(Map.of("status", "rejected"));
    }
    
    // Get group members
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMember>> getMembers(
            @PathVariable String groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(groupService.getGroupMembers(groupId, page, size));
    }
    
    // Delete group (owner only)
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Map<String, String>> deleteGroup(@PathVariable String groupId) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }
    
    // Get single group post detail
    @GetMapping("/group-posts/{postId}")
    public ResponseEntity<GroupPost> getGroupPost(@PathVariable String postId) {
        return ResponseEntity.ok(groupService.getGroupPostById(postId));
    }
    
    // Get comments for a group post
    @GetMapping("/group-posts/{postId}/comments")
    public ResponseEntity<List<com.example.runningapp.social.Comment>> getGroupPostComments(
            @PathVariable String postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return ResponseEntity.ok(groupService.getGroupPostComments(postId, page, size));
    }
    
    // Add comment to group post
    @PostMapping("/group-posts/{postId}/comments")
    public ResponseEntity<com.example.runningapp.social.Comment> addGroupPostComment(
            @PathVariable String postId,
            @RequestBody Map<String, String> request) {
        String content = request.get("contentText");
        String parentCommentId = request.get("parentCommentId");
        return ResponseEntity.ok(groupService.addGroupPostComment(postId, content, parentCommentId));
    }
    
    // Like/Unlike group post
    @PostMapping("/group-posts/{postId}/like")
    public ResponseEntity<GroupPost> likeGroupPost(@PathVariable String postId) {
        return ResponseEntity.ok(groupService.likeGroupPost(postId));
    }
    
    @PostMapping("/group-posts/{postId}/unlike")
    public ResponseEntity<GroupPost> unlikeGroupPost(@PathVariable String postId) {
        return ResponseEntity.ok(groupService.unlikeGroupPost(postId));
    }
    
    // Delete group post
    @DeleteMapping("/group-posts/{postId}")
    public ResponseEntity<Map<String, String>> deleteGroupPost(@PathVariable String postId) {
        groupService.deleteGroupPost(postId);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }
}

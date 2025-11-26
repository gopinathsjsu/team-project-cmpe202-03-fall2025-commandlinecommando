package com.commandlinecommandos.campusmarketplace.communication.controller;

import com.commandlinecommandos.campusmarketplace.communication.model.Conversation;
import com.commandlinecommandos.campusmarketplace.communication.model.Message;
import com.commandlinecommandos.campusmarketplace.communication.repository.ConversationRepository;
import com.commandlinecommandos.campusmarketplace.communication.repository.MessageRepository;
import com.commandlinecommandos.campusmarketplace.model.*;
import com.commandlinecommandos.campusmarketplace.repository.ProductRepository;
import com.commandlinecommandos.campusmarketplace.repository.UserRepository;
import com.commandlinecommandos.campusmarketplace.repository.UniversityRepository;
import com.commandlinecommandos.campusmarketplace.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for ChatController
 * Tests UUID-based conversation and messaging functionality
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ChatControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private University testUniversity;
    private User seller;
    private User buyer;
    private String sellerToken;
    private String buyerToken;
    private Product testListing;
    private Conversation testConversation;

    @BeforeEach
    public void setup() {
        // Clean up
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
        universityRepository.deleteAll();

        // Create test university
        testUniversity = new University();
        testUniversity.setName("Test University");
        testUniversity.setDomain("test.edu");
        testUniversity.setActive(true);
        testUniversity = universityRepository.save(testUniversity);

        // Create seller
        seller = new User();
        seller.setUsername("seller");
        seller.setEmail("seller@test.edu");
        seller.setPassword(passwordEncoder.encode("password123"));
        seller.setFirstName("Seller");
        seller.setLastName("User");
        seller.setRole(UserRole.STUDENT);
        seller.setUniversity(testUniversity);
        seller.setEmailVerifiedAt(java.time.LocalDateTime.now());
        seller.setActive(true);
        seller = userRepository.save(seller);

        // Create buyer
        buyer = new User();
        buyer.setUsername("buyer");
        buyer.setEmail("buyer@test.edu");
        buyer.setPassword(passwordEncoder.encode("password123"));
        buyer.setFirstName("Buyer");
        buyer.setLastName("User");
        buyer.setRole(UserRole.STUDENT);
        buyer.setUniversity(testUniversity);
        buyer.setEmailVerifiedAt(java.time.LocalDateTime.now());
        buyer.setActive(true);
        buyer = userRepository.save(buyer);

        // Generate JWT tokens
        sellerToken = jwtUtil.generateAccessToken(seller);
        buyerToken = jwtUtil.generateAccessToken(buyer);

        // Create test listing
        testListing = new Product();
        testListing.setSeller(seller);
        testListing.setUniversity(testUniversity);
        testListing.setTitle("Test Product for Chat");
        testListing.setDescription("Chat Test Description");
        testListing.setCategory(ProductCategory.ELECTRONICS);
        testListing.setCondition(ProductCondition.NEW);
        testListing.setPrice(BigDecimal.valueOf(99.99));
        testListing.setModerationStatus(ModerationStatus.APPROVED);
        testListing.setActive(true);
        testListing.publish();
        testListing = productRepository.save(testListing);

        // Create test conversation
        testConversation = new Conversation(
            testListing.getProductId(),
            buyer.getUserId(),
            seller.getUserId()
        );
        testConversation = conversationRepository.save(testConversation);
    }

    @Test
    public void testCreateConversation_Success() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("listingId", testListing.getProductId().toString());
        request.put("sellerId", seller.getUserId().toString());

        mockMvc.perform(post("/api/chat/conversations")
                .header("Authorization", "Bearer " + buyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId", notNullValue()))
                .andExpect(jsonPath("$.listingId", is(testListing.getProductId().toString())))
                .andExpect(jsonPath("$.buyerId", is(buyer.getUserId().toString())))
                .andExpect(jsonPath("$.sellerId", is(seller.getUserId().toString())));
    }

    @Test
    public void testGetUserConversations_Success() throws Exception {
        mockMvc.perform(get("/api/chat/conversations")
                .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].conversationId", is(testConversation.getConversationId().toString())));
    }

    @Test
    public void testGetUserConversations_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/chat/conversations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testSendMessage_Success() throws Exception {
        Map<String, Object> messageRequest = new HashMap<>();
        messageRequest.put("conversationId", testConversation.getConversationId().toString());
        messageRequest.put("content", "Hello, is this still available?");

        mockMvc.perform(post("/api/chat/messages")
                .header("Authorization", "Bearer " + buyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId", notNullValue()))
                .andExpect(jsonPath("$.content", is("Hello, is this still available?")))
                .andExpect(jsonPath("$.senderId", is(buyer.getUserId().toString())))
                .andExpect(jsonPath("$.conversationId", is(testConversation.getConversationId().toString())));
    }

    @Test
    public void testGetConversationMessages_Success() throws Exception {
        // First create a message
        Message message = new Message();
        message.setConversation(testConversation);
        message.setSenderId(buyer.getUserId());
        message.setContent("Test message");
        message.setIsRead(false);
        messageRepository.save(message);

        mockMvc.perform(get("/api/chat/conversations/" + testConversation.getConversationId() + "/messages")
                .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].content", is("Test message")));
    }

    @Test
    public void testSendMessage_NotParticipant() throws Exception {
        // Create another user who's not part of the conversation
        User outsider = new User();
        outsider.setUsername("outsider");
        outsider.setEmail("outsider@test.edu");
        outsider.setPassword(passwordEncoder.encode("password123"));
        outsider.setRole(UserRole.STUDENT);
        outsider.setUniversity(testUniversity);
        outsider.setEmailVerifiedAt(java.time.LocalDateTime.now());
        outsider.setActive(true);
        outsider = userRepository.save(outsider);

        String outsiderToken = jwtUtil.generateAccessToken(outsider);

        Map<String, Object> messageRequest = new HashMap<>();
        messageRequest.put("conversationId", testConversation.getConversationId().toString());
        messageRequest.put("content", "I shouldn't be able to send this");

        mockMvc.perform(post("/api/chat/messages")
                .header("Authorization", "Bearer " + outsiderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetUnreadCount_Success() throws Exception {
        // Create unread messages
        Message unreadMessage1 = new Message();
        unreadMessage1.setConversation(testConversation);
        unreadMessage1.setSenderId(seller.getUserId());
        unreadMessage1.setContent("Unread message 1");
        unreadMessage1.setIsRead(false);
        messageRepository.save(unreadMessage1);

        Message unreadMessage2 = new Message();
        unreadMessage2.setConversation(testConversation);
        unreadMessage2.setSenderId(seller.getUserId());
        unreadMessage2.setContent("Unread message 2");
        unreadMessage2.setIsRead(false);
        messageRepository.save(unreadMessage2);

        mockMvc.perform(get("/api/chat/unread-count")
                .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount", greaterThanOrEqualTo(2)));
    }

    @Test
    public void testMarkAsRead_Success() throws Exception {
        // Create unread message
        Message unreadMessage = new Message();
        unreadMessage.setConversation(testConversation);
        unreadMessage.setSenderId(seller.getUserId());
        unreadMessage.setContent("Unread message");
        unreadMessage.setIsRead(false);
        unreadMessage = messageRepository.save(unreadMessage);

        mockMvc.perform(put("/api/chat/messages/" + unreadMessage.getMessageId() + "/read")
                .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk());

        // Verify message is marked as read
        Message updatedMessage = messageRepository.findById(unreadMessage.getMessageId()).orElse(null);
        assert updatedMessage != null;
        assert updatedMessage.getIsRead();
    }
}

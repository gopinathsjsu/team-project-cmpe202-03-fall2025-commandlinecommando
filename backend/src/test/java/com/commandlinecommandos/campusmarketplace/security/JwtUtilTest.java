package com.commandlinecommandos.campusmarketplace.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.model.UserRole;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "jwt.secret=testSecretKeyThatIsLongEnoughForHS256AlgorithmToWorkProperly",
    "jwt.access-token.expiration=3600000",
    "jwt.refresh-token.expiration=604800000"
})
class JwtUtilTest {
    
    private JwtUtil jwtUtil;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Use reflection to set the private fields for testing
        try {
            var secretField = JwtUtil.class.getDeclaredField("secret");
            secretField.setAccessible(true);
            secretField.set(jwtUtil, "testSecretKeyThatIsLongEnoughForHS256AlgorithmToWorkProperly");
            
            var accessTokenExpirationField = JwtUtil.class.getDeclaredField("accessTokenExpiration");
            accessTokenExpirationField.setAccessible(true);
            accessTokenExpirationField.set(jwtUtil, 3600000L);
            
            var refreshTokenExpirationField = JwtUtil.class.getDeclaredField("refreshTokenExpiration");
            refreshTokenExpirationField.setAccessible(true);
            refreshTokenExpirationField.set(jwtUtil, 604800000L);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(UserRole.STUDENT);
    }
    
    @Test
    void testGenerateAccessToken() {
        String token = jwtUtil.generateAccessToken(testUser);
        
        assertNotNull(token);
        assertTrue(token.length() > 0);
        
        // Verify token contains expected claims
        String username = jwtUtil.extractUsername(token);
        assertEquals("testuser", username);
        
        UserRole role = jwtUtil.extractRole(token);
        assertEquals(UserRole.STUDENT, role);
        
        Long userId = jwtUtil.extractUserId(token);
        assertEquals(1L, userId);
    }
    
    @Test
    void testGenerateRefreshToken() {
        String token = jwtUtil.generateRefreshToken(testUser);
        
        assertNotNull(token);
        assertTrue(token.length() > 0);
        
        String username = jwtUtil.extractUsername(token);
        assertEquals("testuser", username);
    }
    
    @Test
    void testValidateToken() {
        String token = jwtUtil.generateAccessToken(testUser);
        
        assertTrue(jwtUtil.validateToken(token, testUser));
    }
    
    @Test
    void testValidateTokenWithWrongUser() {
        String token = jwtUtil.generateAccessToken(testUser);
        
        User wrongUser = new User();
        wrongUser.setUsername("wronguser");
        
        assertFalse(jwtUtil.validateToken(token, wrongUser));
    }
    
    @Test
    void testValidateTokenFormat() {
        String validToken = jwtUtil.generateAccessToken(testUser);
        assertTrue(jwtUtil.validateToken(validToken));
        
        String invalidToken = "invalid.token.format";
        assertFalse(jwtUtil.validateToken(invalidToken));
    }
    
    @Test
    void testExtractExpiration() {
        String token = jwtUtil.generateAccessToken(testUser);
        
        var expiration = jwtUtil.extractExpiration(token);
        assertNotNull(expiration);
        assertTrue(expiration.getTime() > System.currentTimeMillis());
    }
}

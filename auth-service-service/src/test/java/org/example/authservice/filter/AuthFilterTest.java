package org.example.authservice.filter;//package org.example.authservice.filter;

import org.example.authservice.Application;
import org.example.authservice.domain.dto.AuthenticateResponse;
import org.example.authservice.domain.dto.AuthenticationRequest;
import org.example.authservice.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.DEFINED_PORT, classes = Application.class)
@ActiveProfiles(profiles = "test")
@TestPropertySource(properties = "server.port=7777")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(TestSecurityConfiguration.class)
public class AuthFilterTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void afterEach(){
        userRepository.deleteAll();
    }


    @Test
    void shouldResponseWith200IfUserHasValidToken(){
        // given
        String token = registerUser(AuthenticationRequest.builder().email("email").password("test").build()).getAccessToken();

        // when
        var response = sendRequestToTestController(token);

        // then
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals("ok", response.getBody());
    }

    @Test
    void shouldResponseWith403IfUserHasInvalidToken(){
        // given and when
        var response = sendRequestToTestController("invalid");

        // then
        Assertions.assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(403));
    }

    @Test
    void shouldResponseWith403IfUserUsesNonExistingToken(){
        // given
        String email = "email";
        String token = registerUser(AuthenticationRequest.builder().email(email).password("test").build()).getAccessToken();
        var user = userRepository.findByEmail("email");
        userRepository.delete(user.get());

        var response = sendRequestToTestController(token);

        // then
        Assertions.assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(403));
    }

    private ResponseEntity<String> sendRequestToTestController(String token){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization","Bearer " + token);
        var response = testRestTemplate.exchange("http://localhost:7777/api/v1/test", HttpMethod.POST,
                new HttpEntity<>(headers), String.class);
        return response;
    }
    private AuthenticateResponse registerUser(AuthenticationRequest authenticationRequest){
        var registerResponse = testRestTemplate.postForEntity("/api/v1/auth/register", authenticationRequest, AuthenticateResponse.class);
        Assertions.assertTrue(registerResponse.getStatusCode().is2xxSuccessful());
        return registerResponse.getBody();
    }

}

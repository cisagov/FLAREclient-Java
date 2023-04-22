package com.bcmc.xor.flare.client.api.service;

import com.bcmc.xor.flare.client.api.FlareclientApp;
import com.bcmc.xor.flare.client.api.config.Constants;
import com.bcmc.xor.flare.client.api.domain.auth.User;
import com.bcmc.xor.flare.client.api.repository.UserRepository;
import com.bcmc.xor.flare.client.api.service.dto.UserDTO;
import com.bcmc.xor.flare.client.util.RandomUtil;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for the UserResource REST controller.
 *
 * @see UserService
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = FlareclientApp.class)
public class UserServiceIntTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private User user;

    @Before
    public void init() {
        userRepository.deleteAll();
        user = new User();
        user.setId("123");
        user.setLogin("johndoe");
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setEmail("johndoe@localhost");
        user.setFirstName("john");
        user.setLastName("doe");
        user.setImageUrl("http://placehold.it/50x50");
        user.setLangKey("en");
        user.setActivationKey("testKey");
    }

    @Test
    public void assertThatUserMustExistToResetPassword() {
        userRepository.save(user);
        Optional<User> maybeUser = userService.requestPasswordReset("invalid.login@localhost");
        assertThat(maybeUser).isNotPresent();

        maybeUser = userService.requestPasswordReset(user.getEmail());
        assertThat(maybeUser).isPresent();
        assertThat(maybeUser.orElse(null).getEmail()).isEqualTo(user.getEmail());
        assertThat(maybeUser.orElse(null).getResetDate()).isNotNull();
        assertThat(maybeUser.orElse(null).getResetKey()).isNotNull();
    }

    @Test
    public void assertThatOnlyActivatedUserCanRequestPasswordReset() {
        user.setActivated(false);
        userRepository.save(user);

        Optional<User> maybeUser = userService.requestPasswordReset(user.getLogin());
        assertThat(maybeUser).isNotPresent();
        userRepository.delete(user);
    }

    @Test
    public void assertThatResetKeyMustNotBeOlderThan24Hours() {
        Instant daysAgo = Instant.now().minus(25, ChronoUnit.HOURS);
        String resetKey = RandomUtil.generateResetKey();
        user.setActivated(true);
        user.setResetDate(daysAgo);
        user.setResetKey(resetKey);
        userRepository.save(user);

        Optional<User> maybeUser = userService.completePasswordReset("johndoe2", user.getResetKey());
        assertThat(maybeUser).isNotPresent();
        userRepository.delete(user);
    }

    @Test
    public void assertThatResetKeyMustBeValid() {
        Instant daysAgo = Instant.now().minus(25, ChronoUnit.HOURS);
        user.setActivated(true);
        user.setResetDate(daysAgo);
        user.setResetKey("1234");
        userRepository.save(user);

        Optional<User> maybeUser = userService.completePasswordReset("johndoe2", user.getResetKey());
        assertThat(maybeUser).isNotPresent();
        userRepository.delete(user);
    }

    @Test
    public void assertThatUserCanResetPassword() {
        String oldPassword = user.getPassword();
        Instant daysAgo = Instant.now().minus(2, ChronoUnit.HOURS);
        String resetKey = RandomUtil.generateResetKey();
        user.setActivated(true);
        user.setResetDate(daysAgo);
        user.setResetKey(resetKey);
        userRepository.save(user);

        Optional<User> maybeUser = userService.completePasswordReset("johndoe2", user.getResetKey());
        assertThat(maybeUser).isPresent();
        assertThat(maybeUser.orElse(null).getResetDate()).isNull();
        assertThat(maybeUser.orElse(null).getResetKey()).isNull();
        assertThat(maybeUser.orElse(null).getPassword()).isNotEqualTo(oldPassword);

        userRepository.delete(user);
    }

    @Test
    public void testFindNotActivatedUsersByCreationDateBefore() {
        Instant now = Instant.now();
        user.setActivated(false);
        User dbUser = userRepository.save(user);
        dbUser.setCreatedDate(now.minus(4, ChronoUnit.DAYS));
        userRepository.save(user);
        List<User> users = userRepository.findAllByActivatedIsFalseAndCreatedDateBefore(now.minus(3, ChronoUnit.DAYS));
        assertThat(users).isNotEmpty();
        userService.removeNotActivatedUsers();
        users = userRepository.findAllByActivatedIsFalseAndCreatedDateBefore(now.minus(3, ChronoUnit.DAYS));
        assertThat(users).isEmpty();
    }

    @Test
    public void assertThatAnonymousUserIsNotGet() {
        user.setLogin(Constants.ANONYMOUS_USER);
        if (!userRepository.findOneByLogin(Constants.ANONYMOUS_USER).isPresent()) {
            userRepository.save(user);
        }
        final PageRequest pageable = PageRequest.of(0, (int) userRepository.count());
        final Page<UserDTO> allManagedUsers = userService.getAllManagedUsers(pageable);
        assertThat(allManagedUsers.getContent().stream()
            .noneMatch(user -> Constants.ANONYMOUS_USER.equals(user.getLogin())))
            .isTrue();
    }


    @Test
    public void testRemoveNotActivatedUsers() {

        user.setActivated(false);
        userRepository.save(user);
        // Let the audit first set the creation date but then update it
        user.setCreatedDate(Instant.now().minus(30, ChronoUnit.DAYS));
        userRepository.save(user);

        assertThat(userRepository.findOneByLogin("johndoe")).isPresent();
        userService.removeNotActivatedUsers();
        assertThat(userRepository.findOneByLogin("johndoe")).isNotPresent();
    }

    @Test
    public void testActiveResgistration() {
        userRepository.save(user);
        Optional<User> maybeUser = userService.activateRegistration("testKey");

        assertTrue(maybeUser.isPresent());
    }

    @Test
    public void testRegisterUser() {
        UserDTO userDTO = new UserDTO(user);
        User registeredUser = userService.registerUser(userDTO, user.getPassword());

        Optional<User> maybeUser = userRepository.findById(registeredUser.getId());

        assertTrue(maybeUser.isPresent());
        assertEquals(user.getLogin(), maybeUser.get().getLogin());
        assertEquals(user.getEmail(), maybeUser.get().getEmail());
        assertEquals(user.getFirstName(), maybeUser.get().getFirstName());
        assertEquals(user.getLastName(), maybeUser.get().getLastName());
        assertEquals(user.getImageUrl(), maybeUser.get().getImageUrl());
    }

    @Test
    public void testCreateUser() {
        UserDTO userDTO = new UserDTO(user);
        User createdUser = userService.createUser(userDTO);

        Optional<User> maybeUser = userRepository.findById(createdUser.getId());

        assertTrue(maybeUser.isPresent());
        assertEquals(user.getLogin(), maybeUser.get().getLogin());
        assertEquals(user.getEmail(), maybeUser.get().getEmail());
        assertEquals(user.getFirstName(), maybeUser.get().getFirstName());
        assertEquals(user.getLastName(), maybeUser.get().getLastName());
        assertEquals(user.getImageUrl(), maybeUser.get().getImageUrl());
    }

    @Test
    public void testUpdateUser() {
        String login = "test";
        String firstName = "FooBar";
        String lastName = "Baz";
        String email = "test@mail.com";
        String imageURL = "127.0.0.1:8080";
        boolean activated = false;
        String langKey = "Korean";

        UserDTO userDTO = new UserDTO(user);
        User createdUser = userService.createUser(userDTO);

        UserDTO createdUserDTO = new UserDTO(createdUser);

        createdUserDTO.setLogin(login);
        createdUserDTO.setFirstName(firstName);
        createdUserDTO.setLastName(lastName);
        createdUserDTO.setEmail(email);
        createdUserDTO.setImageUrl(imageURL);
        createdUserDTO.setActivated(activated);
        createdUserDTO.setLangKey(langKey);


        Optional<UserDTO> maybeUser = userService.updateUser(createdUserDTO);

        assertTrue(maybeUser.isPresent());
        assertEquals(login, maybeUser.get().getLogin());
        assertEquals(firstName, maybeUser.get().getFirstName());
        assertEquals(lastName, maybeUser.get().getLastName());
        assertEquals(email, maybeUser.get().getEmail());
        assertEquals(imageURL, maybeUser.get().getImageUrl());
        assertEquals(activated, maybeUser.get().isActivated());
        assertEquals(langKey, maybeUser.get().getLangKey());
    }

    @Test
    public void testDeleteUser() {
        UserDTO userDTO = new UserDTO(user);
        User createdUser = userService.createUser(userDTO);
        assertTrue(userRepository.findById(createdUser.getId()).isPresent());

        userService.deleteUser(createdUser.getLogin());

        assertFalse(userRepository.findById(createdUser.getId()).isPresent());
    }

    @Test
    public void testGetUserWithAuthorities() {
        UserDTO userDTO = new UserDTO(user);
        User createdUser = userService.createUser(userDTO);
        assertTrue(userRepository.findById(createdUser.getId()).isPresent());

        Optional<User> maybeUser = userService.getUserWithAuthorities(createdUser.getId());

        assertTrue(maybeUser.isPresent());
        assertEquals(createdUser.getLogin(), maybeUser.get().getLogin());
        assertEquals(createdUser.getEmail(), maybeUser.get().getEmail());
        assertEquals(createdUser.getFirstName(), maybeUser.get().getFirstName());
        assertEquals(createdUser.getLastName(), maybeUser.get().getLastName());
        assertEquals(createdUser.getImageUrl(), maybeUser.get().getImageUrl());
    }

    @Test
    public void testGetUserWithAuthoritiesByLogin() {
        UserDTO userDTO = new UserDTO(user);
        User createdUser = userService.createUser(userDTO);
        assertTrue(userRepository.findById(createdUser.getId()).isPresent());

        Optional<User> maybeUser = userService.getUserWithAuthoritiesByLogin(createdUser.getLogin());

        assertTrue(maybeUser.isPresent());
        assertEquals(user.getLogin(), maybeUser.get().getLogin());
        assertEquals(user.getEmail(), maybeUser.get().getEmail());
        assertEquals(user.getFirstName(), maybeUser.get().getFirstName());
        assertEquals(user.getLastName(), maybeUser.get().getLastName());
        assertEquals(user.getImageUrl(), maybeUser.get().getImageUrl());
    }
}

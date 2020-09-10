package com.bcmc.xor.flare.client.api.web.rest;

import com.bcmc.xor.flare.client.api.config.Constants;
import com.bcmc.xor.flare.client.api.domain.auth.User;
import com.bcmc.xor.flare.client.api.security.SecurityUtils;
import com.bcmc.xor.flare.client.api.service.MailService;
import com.bcmc.xor.flare.client.api.service.UserService;
import com.bcmc.xor.flare.client.api.service.dto.UserDTO;
import com.bcmc.xor.flare.client.error.*;
import com.bcmc.xor.flare.client.util.PaginationUtil;
import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

/**
 * REST controller for managing users.
 * <p>
 * This class accesses the User entity, and needs to fetch its collection of authorities.
 * <p>
 * For a normal use-case, it would be better to have an eager relationship between User and Authority,
 * and send everything to the client side: there would be no View Model and DTO, a lot less code, and an outer-join
 * which would be good for performance.
 * <p>
 * We use a View Model and a DTO for 3 reasons:
 * <ul>
 * <li>We want to keep a lazy association between the user and the authorities, because people will
 * quite often do relationships with the user, and we don't want them to get the authorities all
 * the time for nothing (for performance reasons). This is the #1 goal: we should not impact our users'
 * application because of this use-case.</li>
 * <li> Not having an outer join causes n+1 requests to the database. This is not a real issue as
 * we have by default a second-level cache. This means on the first HTTP call we do the n+1 requests,
 * but then all authorities come from the cache, so in fact it's much better than doing an outer join
 * (which will get lots of data from the database, for each HTTP call).</li>
 * <li> As this manages users, for security reasons, we'd rather have a DTO layer.</li>
 * </ul>
 * <p>
 * Another option would be to have a specific JPA entity graph to handle this case.
 */
@RestController
@RequestMapping("/api")
public class UserResource {

    private static final Logger log = LoggerFactory.getLogger(UserResource.class);

    private final UserService userService;
    private final MailService mailService;

    public UserResource(UserService userService, MailService mailService) {

        this.userService = userService;
        this.mailService = mailService;
    }

    /**
     * POST  /users  : Creates a new user.
     * <p>
     * Creates a new user if the login and email are not already used, and sends an
     * mail with an activation link.
     * The user needs to be activated on creation.
     *
     * @param userDTO the user to create
     * @return the ResponseEntity with status 201 (Created) and with body the new user, or with status 400 (Bad Request) if the login or email is already in use
     */
    @PostMapping("/users")
    @Timed
    public ResponseEntity<User> createUser(@Valid @RequestBody UserDTO userDTO) {
        log.debug("REST request to create User : {}", userDTO);

        if(!SecurityUtils.isCurrentUserInRole("ROLE_ADMIN")) {
            throw new AuthenticationFailureException();
        }

        if (userDTO.getId() != null) {
            Map<String,Object> badParamMap = new HashMap<>();
            badParamMap.put("id", ErrorConstants.ILLEGAL_ARG_USER_ID);
            log.error(ErrorConstants.ILLEGAL_ARG_USER_ID);
            throw new FlareClientIllegalArgumentException(badParamMap);
            // Lowercase the user login before comparing with database
        } else if (userService.getUserWithAuthoritiesByLogin(userDTO.getLogin().toLowerCase()).isPresent()) {
            log.error("Login already users for {}", userDTO.getLogin());
            throw new LoginAlreadyUsedException();
        } else if (userService.getUserByEmail(userDTO.getEmail()).isPresent()) {
            log.error("Email already used for {}", userDTO.getEmail());
            throw new EmailAlreadyUsedException();
        } else {
            User newUser = userService.createUser(userDTO);
            mailService.sendCreationEmail(newUser);
            log.debug("User created: {}", newUser);
            return new ResponseEntity<>(newUser, HttpStatus.CREATED);
        }
    }

    /**
     * PUT /users : Updates an existing User.
     *
     * @param userDTO the user to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated user
     * @throws EmailAlreadyUsedException 400 (Bad Request) if the email is already in use
     * @throws LoginAlreadyUsedException 400 (Bad Request) if the login is already in use
     */
    @PutMapping("/users")
    @Timed
    public ResponseEntity<UserDTO> updateUser(@Valid @RequestBody UserDTO userDTO) {
        log.debug("REST request to update User : {}", userDTO);

        if(!SecurityUtils.isCurrentUserInRole("ROLE_ADMIN")) {
            throw new AuthenticationFailureException();
        }

        if (userDTO.getId() == null) {
            Map<String,Object> badParamMap = new HashMap<>();
            badParamMap.put("id", ErrorConstants.ILLEGAL_ARG_MISSING_USER_ID);
            log.error(ErrorConstants.ILLEGAL_ARG_MISSING_USER_ID);
            throw new FlareClientIllegalArgumentException(badParamMap);
        }

        Optional<User> existingUser = userService.getUserByEmail(userDTO.getEmail());
        if (existingUser.isPresent() && (!existingUser.get().getId().equals(userDTO.getId()))) {
            log.error("Email already used for {}", userDTO.getEmail());
            throw new EmailAlreadyUsedException();
        }

        // Lowercase the user login before comparing with database
        existingUser = userService.getUserWithAuthoritiesByLogin(userDTO.getLogin().toLowerCase());
        if (existingUser.isPresent() && (!existingUser.get().getId().equals(userDTO.getId()))) {
            log.error("Login already users for {}", userDTO.getLogin());
            throw new LoginAlreadyUsedException();
        }

        Optional<UserDTO> updatedUser = userService.updateUser(userDTO);
        if (!updatedUser.isPresent()) {
            log.error("Updated user not returned from service.  Check logs for errors.");
            throw new FlareServiceUnavailableException();
        }

        log.debug("User updated: {}", updatedUser);
        return new ResponseEntity<>(updatedUser.get(), HttpStatus.OK);
    }

    /**
     * GET /users : get all users.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and with body all users
     */
    @GetMapping("/users")
    @Timed
    public ResponseEntity<List<UserDTO>> getAllUsers(Pageable pageable) {
        log.debug("REST request to get all users");

        final Page<UserDTO> page = userService.getAllManagedUsers(pageable);
        // Headers used for pagination
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/users");

        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * @return a string list of the all of the roles
     */
    @GetMapping("/users/authorities")
    @Timed
    public List<String> getAuthorities() {
        log.debug("REST request to get all authorities");

        return userService.getAuthorities();
    }

    /**
     * GET /users/:login : get the "login" user.
     *
     * @param login the login of the user to find
     * @return the ResponseEntity with status 200 (OK) and with body the "login" user, or with status 404 (Not Found)
     */
    @GetMapping("/users/{login:" + Constants.LOGIN_REGEX + "}")
    @Timed
    public ResponseEntity<UserDTO> getUser(@PathVariable String login) {
        log.debug("REST request to get User : {}", login);

        if (!userService.getUserWithAuthoritiesByLogin(login).isPresent()) {
            log.error(ErrorConstants.USER_NOT_FOUND);
            throw new UserNotFoundException();
        }
        User user = userService.getUserWithAuthoritiesByLogin(login).get();
        return new ResponseEntity<>(new UserDTO(user), HttpStatus.OK);
    }

    /**
     * DELETE /users/:login : delete the "login" User.
     *
     * @param login the login of the user to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/users/{login:" + Constants.LOGIN_REGEX + "}")
    @Timed
    public ResponseEntity<String> deleteUser(@PathVariable String login) {
        log.debug("REST request to delete User: {}", login);

        if(!SecurityUtils.isCurrentUserInRole("ROLE_ADMIN")) {
            throw new AuthenticationFailureException();
        }

        if (!userService.getUserWithAuthoritiesByLogin(login).isPresent()) {
            log.error(ErrorConstants.USER_NOT_FOUND);
            throw new UserNotFoundException();
        }

        userService.deleteUser(login);
        String message = "User successfully deleted for login: " + login;
        log.debug(message);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }
}

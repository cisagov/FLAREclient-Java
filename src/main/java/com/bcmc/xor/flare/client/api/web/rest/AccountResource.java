package com.bcmc.xor.flare.client.api.web.rest;

import com.bcmc.xor.flare.client.api.domain.auth.User;
import com.bcmc.xor.flare.client.api.repository.UserRepository;
import com.bcmc.xor.flare.client.api.security.SecurityUtils;
import com.bcmc.xor.flare.client.api.service.MailService;
import com.bcmc.xor.flare.client.api.service.UserService;
import com.bcmc.xor.flare.client.api.service.dto.PasswordChangeDTO;
import com.bcmc.xor.flare.client.api.service.dto.UserDTO;
import com.bcmc.xor.flare.client.api.web.rest.vm.KeyAndPasswordVM;
import com.bcmc.xor.flare.client.api.web.rest.vm.ManagedUserVM;
import com.bcmc.xor.flare.client.error.*;
import com.codahale.metrics.annotation.Timed;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Optional;

/**
 * REST controller for managing the current user's account.
 */
@SuppressWarnings("unused")
@RestController
@RequestMapping("/api")
public class AccountResource {

    private static final Logger log = LoggerFactory.getLogger(AccountResource.class);

    private final UserRepository userRepository;

    private final UserService userService;

    private final MailService mailService;

    public AccountResource(UserRepository userRepository, UserService userService, MailService mailService) {

        this.userRepository = userRepository;
        this.userService = userService;
        this.mailService = mailService;
    }

    /**
     * POST  /register : register the user.
     *
     * @param managedUserVM the managed user View Model
     * @throws InvalidPasswordException 400 (Bad Request) if the password is incorrect
     * @throws EmailAlreadyUsedException 409 (Conflict) if the email is already used
     * @throws LoginAlreadyUsedException 409 (Conflict) if the login is already used
     */

    @SuppressWarnings("unchecked")
	@PostMapping("/register")
	@Timed
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public Object registerAccount(@Valid @RequestBody ManagedUserVM managedUserVM) {

		HttpHeaders httpHeaders = new HttpHeaders();
		User user = new User();
		
		try {
	
			if (checkPasswordLength(managedUserVM.getPassword())) {throw new InvalidPasswordException();}
			
			userRepository.findOneByLogin(managedUserVM.getLogin().toLowerCase()).ifPresent(u -> { throw new LoginAlreadyUsedException();});
			userRepository.findOneByEmailIgnoreCase(managedUserVM.getEmail()).ifPresent(u -> {throw new EmailAlreadyUsedException();});

			user = userService.registerUser(managedUserVM, managedUserVM.getPassword());
			mailService.sendActivationEmail(user);
			
			httpHeaders.add("api-register", "created");
			return new ResponseEntity (user, httpHeaders, HttpStatus.CREATED);
			
		} catch (Exception e) {
			
			switch(e.getClass().getSimpleName()) {
			case "LoginAlreadyUsedException":
				httpHeaders.add("api-register", ErrorConstants.ERR_LOGIN_IN_USED);
				return new ResponseEntity (new LoginAlreadyUsedException(), httpHeaders, HttpStatus.CONFLICT);
			case "EmailAlreadyUsedException":
				httpHeaders.add("api-register", ErrorConstants.ERR_EMAIL_IN_USED);
				return new ResponseEntity (new EmailAlreadyUsedException(), httpHeaders, HttpStatus.CONFLICT);
			default:
				httpHeaders.add("api-register", ErrorConstants.ERR_BAD_REQUEST);
				return new ResponseEntity (e, httpHeaders, HttpStatus.BAD_REQUEST);
			}
		}
	}

    /**
     * GET  /activate : activate the registered user.
     *
     * @param key the activation key
     * @throws RuntimeException 500 (Internal Server Error) if the user couldn't be activated
     */

    @GetMapping("/activate")
    @Timed
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public Object activateAccount(@RequestParam(value = "key") String key) {

		HttpHeaders httpHeaders = new HttpHeaders();
		
		try {
			Optional<User> user = userService.activateRegistration(key);
			if (!user.isPresent()) {
				throw new NotFoundException("No user was found for this reset key");
			}
			
			httpHeaders.add("api-activate", "actived");
			return new ResponseEntity(user, httpHeaders, HttpStatus.CREATED);
			
		} catch (Exception e) {	
			httpHeaders.add("api-activate", ErrorConstants.ERR_ACTIVATIONKEY_NOT_FOUND);
			return new ResponseEntity(e, httpHeaders, HttpStatus.BAD_REQUEST);
		}
	}

    /**
     * GET  /authenticate : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request
     * @return the login if the user is authenticated
     */
    @GetMapping("/authenticate")
    @Timed
    public String isAuthenticated(HttpServletRequest request) {
        log.debug("REST request to check if the current user {} is authenticated", request.getRemoteUser());
        return request.getRemoteUser();
    }

    /**
     * GET  /account : get the current user.
     *
     * @return the current user
     * @throws RuntimeException 500 (Internal Server Error) if the user couldn't be returned
     */
    @GetMapping("/account")
    @Timed
    public UserDTO getAccount() {
        return userService.getUserWithAuthorities()
            .map(UserDTO::new)
            .orElseThrow(() -> new NotFoundException("User could not be found"));
    }

    /**
     * POST  /account : update the current user information.
     *
     * @param userDTO the current user information
     * @throws EmailAlreadyUsedException 400 (Bad Request) if the email is already used
     * @throws RuntimeException 500 (Internal Server Error) if the user login wasn't found
     */
    @PostMapping("/account")
    @Timed
    public void saveAccount(@Valid @RequestBody UserDTO userDTO) {
        final String userLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new InternalServerErrorException("Current user login not found"));
        Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());
        if (existingUser.isPresent() && (!existingUser.get().getLogin().equalsIgnoreCase(userLogin))) {
            throw new EmailAlreadyUsedException();
        }
        Optional<User> user = userRepository.findOneByLogin(userLogin);
        if (!user.isPresent()) {
            throw new InternalServerErrorException("User could not be found");
        }
        userService.updateUser(userDTO.getFirstName(), userDTO.getLastName(), userDTO.getEmail(),
            userDTO.getLangKey(), userDTO.getImageUrl());
   }

    /**
     * POST  /account/change-password : changes the current user's password
     *
     * @param passwordChangeDto current and new password
     * @throws InvalidPasswordException 400 (Bad Request) if the new password is incorrect
     */
    @PostMapping(path = "/account/change-password")
    @Timed
    public void changePassword(@RequestBody PasswordChangeDTO passwordChangeDto) {
        if (checkPasswordLength(passwordChangeDto.getNewPassword())) {
            throw new InvalidPasswordException();
        }
        userService.changePassword(passwordChangeDto.getCurrentPassword(), passwordChangeDto.getNewPassword());
   }

    /**
     * POST   /account/reset-password/init : Send an email to reset the password of the user
     *
     * @param mail the mail of the user
     * @throws EmailNotFoundException 400 (Bad Request) if the email address is not registered
     */
    @PostMapping(path = "/account/reset-password/init")
    @Timed
    public void requestPasswordReset(@RequestBody String mail) {
       mailService.sendPasswordResetMail(
           userService.requestPasswordReset(mail)
               .orElseThrow(EmailNotFoundException::new)
       );
    }

    /**
     * POST   /account/reset-password/finish : Finish to reset the password of the user
     *
     * @param keyAndPassword the generated key and the new password
     * @throws InvalidPasswordException 400 (Bad Request) if the password is incorrect
     * @throws RuntimeException 500 (Internal Server Error) if the password could not be reset
     */
    @PostMapping(path = "/account/reset-password/finish")
    @Timed
    public void finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPassword) {
        if (checkPasswordLength(keyAndPassword.getNewPassword())) {
            throw new InvalidPasswordException();
        }
        Optional<User> user =
            userService.completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey());

        if (!user.isPresent()) {
            throw new NotFoundException("No user was found for this reset key");
        }
    }

    private static boolean checkPasswordLength(String password) {
        return StringUtils.isEmpty(password) ||
            password.length() < ManagedUserVM.PASSWORD_MIN_LENGTH ||
            password.length() > ManagedUserVM.PASSWORD_MAX_LENGTH;
    }
}

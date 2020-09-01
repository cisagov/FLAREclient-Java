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

import java.util.ArrayList;
import java.util.Optional;

/**
 * REST controller for managing the current user's account.
 */
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
	 * POST /register : register the user.
	 *
	 * @param managedUserVM the managed user View Model
	 * @throws InvalidPasswordException  400 (Bad Request) if the password is
	 *                                   incorrect
	 * @throws EmailAlreadyUsedException 400 (Bad Request) if the email is already used
	 * @throws LoginAlreadyUsedException 400 (Bad Request) if the login is already used
	 */

	@PostMapping("/register")
	@Timed
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public Object registerAccount(@Valid @RequestBody ManagedUserVM managedUserVM) {

		log.debug("REST request to register a user account");
		  
		userRepository.findOneByLogin(managedUserVM.getLogin().toLowerCase()).ifPresent(u -> {
			log.error("REST API AccountResource register: Exception: LoginAlreadyUsedException ");
			throw new LoginAlreadyUsedException();
		});

		userRepository.findOneByEmailIgnoreCase(managedUserVM.getEmail()).ifPresent(u -> {
			log.error("REST API AccountResource register: Exception: EmailAlreadyUsedException ");
			throw new EmailAlreadyUsedException();
		});

		User user = userService.registerUser(managedUserVM, managedUserVM.getPassword());
		mailService.sendActivationEmail(user);
		return user;
	}

	/**
	 * GET /activate : activate the registered user.
	 *
	 * @param key the activation key
	 * @throws AccountActivationException if the user couldn't be activated
	 */
	@GetMapping("/activate")
	@Timed
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public Object activateAccount(@RequestParam(value = "key") String key) {
		
		log.debug("REST request to activate a user account");
		
		Optional<User> user = userService.activateRegistration(key);
		if (!user.isPresent()) {
			log.error("REST API AccountResource activate: Exception: AccountActivationException ");
			throw new AccountActivationException();
		}

		return user;
	}

    /**
     * GET  /authenticate : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request
     * @return the login if the user is authenticated
     */
    @GetMapping("/authenticate")
	@ResponseStatus(HttpStatus.OK)
    @Timed
    @ResponseBody
    public Object isAuthenticated(HttpServletRequest request) {
        log.debug("REST request to check if the current user {} is authenticated", request.getRemoteUser());
    	
		String login = request.getRemoteUser();
		ArrayList<String> alist = new ArrayList<>();
		alist.add(login);
        return alist;
    }
	
	/**
	 * GET /account : get the current user.
	 *
	 * @return the current user
	 * @throws RuntimeException 500 (Internal Server Error) if the user couldn't be
	 *                          returned
	 */
	@GetMapping("/account")
	@ResponseStatus(HttpStatus.OK)
	@Timed
	@ResponseBody
	public UserDTO getAccount() {
		log.debug("REST request to get current user");
		
		return userService.getUserWithAuthorities().map(UserDTO::new)
				.orElseThrow(() -> new NotFoundException("User could not be found"));
	}

	/**
	 * POST /account : update the current user information.
	 *
	 * @param userDTO the current user information
	 * @throws LoginAlreadyUsedException 409 if the user try to change email
	 * @throws EmailAlreadyUsedException 409 if the email is already used
	 * @throws AccountUpdateException    400 (Bad Request) Other exceptions
	 */
	@PostMapping("/account")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@Timed
	@ResponseBody
	public Object saveAccount(@Valid @RequestBody UserDTO userDTO) {
		
		log.debug("REST request to update a user information");
		
		try {
			final String userLogin = SecurityUtils.getCurrentUserLogin()
					.orElseThrow(() -> new InternalServerErrorException("Current user login not found"));

			Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());
			Optional<User> user = userRepository.findOneByLogin(userLogin);

			if (existingUser.isPresent() && (!existingUser.get().getLogin().equalsIgnoreCase(userLogin))) {
				log.error("REST API AccountResource saveAccount exception: EmailAlreadyUsedException ");
				throw new EmailAlreadyUsedException();
			}

			if (!user.isPresent()) {
				log.error("REST API AccountResource saveAccount exception: AccountUpdateException ");
				throw new AccountUpdateException();
			}

			userService.updateUser(userDTO.getFirstName(), userDTO.getLastName(), userDTO.getEmail(),
					userDTO.getLangKey(), userDTO.getImageUrl());

			return user;

		} catch (Exception e) {
			log.error("REST API AccountResource saveAccount exception: Exception ");
			throw new AccountUpdateException();
		}
	}

	/**
	 * POST /account/change-password : changes the current user's password
	 *
	 * @param passwordChangeDto current and new password
	 * @throws InvalidPasswordException 400 (Bad Request) if the new password is
	 *                                  incorrect
	 *
	 */
	@PostMapping(path = "/account/change-password")
	@Timed
	@ResponseBody
	@ResponseStatus(HttpStatus.ACCEPTED)
	public Object changePassword(@RequestBody PasswordChangeDTO passwordChangeDto) {
		
		log.debug("REST API AccountResource changePassword");
		
		final String userLogin = SecurityUtils.getCurrentUserLogin()
				.orElseThrow(() -> new InternalServerErrorException("Current user login not found"));

		if (checkPasswordLength(passwordChangeDto.getNewPassword())) {
			log.error("REST API AccountResource account-change-password: Exception: InvalidPasswordException");
			throw new InvalidPasswordException();
		}

		userService.changePassword(passwordChangeDto.getCurrentPassword(), passwordChangeDto.getNewPassword());
		return userService.getUserWithAuthoritiesByLogin(userLogin);
		
	}

	/**
	 * POST /account/reset-password/init : Send an email to reset the password of
	 * the user
	 *
	 * @param mail the mail of the user
	 * @throws EmailNotFoundException 400 (Bad Request) if the email address is not
	 *                                registered
	 */
	@PostMapping(path = "/account/reset-password/init")
	@ResponseStatus(HttpStatus.OK)
	@Timed
	@ResponseBody
	public void requestPasswordReset(@RequestBody String mail) {
		
		log.debug("REST API AccountResource requestPasswordReset");
		
		mailService
				.sendPasswordResetMail(userService.requestPasswordReset(mail).orElseThrow(EmailNotFoundException::new));
	}

	/**
	 * POST /account/reset-password/finish : Finish to reset the password of the
	 * user
	 *
	 * @param keyAndPassword the generated key and the new password
	 * @throws InvalidPasswordException 400 (Bad Request) if the password is
	 *                                  incorrect
	 * @throws RuntimeException         500 (Internal Server Error) if the password
	 *                                  could not be reset
	 */
	@PostMapping(path = "/account/reset-password/finish")
	@ResponseStatus(HttpStatus.OK)
	@Timed
	@ResponseBody
	public void finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPassword) {
		
		log.debug("REST API AccountResource finishPasswordReset");
		
		if (checkPasswordLength(keyAndPassword.getNewPassword())) {
			log.error("REST API AccountResource finishPasswordReset exception : InvalidPasswordException");
			throw new InvalidPasswordException();
		}
		
		Optional<User> user = userService.completePasswordReset(keyAndPassword.getNewPassword(),
				keyAndPassword.getKey());

		if (!user.isPresent()) {
			log.error("REST API AccountResource finishPasswordReset exception : NotFoundException");
			throw new NotFoundException("No user was found for this reset key");
		}
	}

	private static boolean checkPasswordLength(String password) {
		return StringUtils.isEmpty(password) || password.length() < ManagedUserVM.PASSWORD_MIN_LENGTH
				|| password.length() > ManagedUserVM.PASSWORD_MAX_LENGTH;
	}
}

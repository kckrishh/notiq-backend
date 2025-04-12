package com.notiq.app.Config.Security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;

import com.notiq.app.Model.User;
import com.notiq.app.Repo.UserRepo;

public class CustomUserDetailsManager implements UserDetailsManager {

    private final UserRepo repo;

    public CustomUserDetailsManager(UserRepo repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email"));
        return new CustomUserDetails(user);
    }

    @Override
    public void createUser(UserDetails userDetails) {
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        // if (repo.findByEmail(customUserDetails.getUsername()).isPresent()) {
        // throw new IllegalArgumentException("User already exists");
        // }
        User newUser = new User();
        newUser.setEmail(customUserDetails.getUsername());
        newUser.setPassword(customUserDetails.getPassword());
        newUser.setEnabled(false);
        newUser.setUsername(customUserDetails.getUser().getUsername());
        newUser.setVerificationToken(UUID.randomUUID().toString());

        repo.save(newUser);
        // return newUSer;
    }

    @Override
    public void updateUser(UserDetails userDetails) {
        User user = repo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("user not found: " + userDetails.getUsername()));

        user.setPassword(userDetails.getPassword());

        user.setUsername(userDetails.getUsername());

        repo.save(user);
    }

    @Override
    public void deleteUser(String username) {
        repo.deleteByEmail(username);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();

        String email = currentAuth.getName();

        User user = repo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (oldPassword == user.getPassword()) {
            throw new IllegalArgumentException("Old password does not match");
        }

        user.setPassword(newPassword);
        repo.save(user);
    }

    @Override
    public boolean userExists(String username) {
        return repo.findByEmail(username).isPresent();
    }

}

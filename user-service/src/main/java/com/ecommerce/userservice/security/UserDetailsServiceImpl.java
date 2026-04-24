package com.ecommerce.userservice.security;

import com.ecommerce.userservice.exception.ResourceNotFoundException;
import com.ecommerce.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetails loadUserByUsername(String email)
        throws UsernameNotFoundException{
        return userRepository.findByEmail(email)
                .map(UserDetailsImpl::new)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "User not found with email:" + email));
    }
}

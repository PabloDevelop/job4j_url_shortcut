package ru.job4j.shortcut.service;

import lombok.AllArgsConstructor;
import net.jcip.annotations.ThreadSafe;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ru.job4j.shortcut.dto.SiteDTO;
import ru.job4j.shortcut.model.Site;
import ru.job4j.shortcut.repository.SiteRepository;
import ru.job4j.shortcut.util.LoginAndUrlKeyGenerator;
import ru.job4j.shortcut.util.PasswordGenerator;

import java.util.Optional;

import static java.util.Collections.emptyList;

@Service
@AllArgsConstructor
@ThreadSafe
public class SimpleSiteService implements SiteService, UserDetailsService {
    private final SiteRepository siteRepository;
    private final PasswordGenerator passwordGenerator;
    private final LoginAndUrlKeyGenerator loginGenerator;
    private final BCryptPasswordEncoder encoder;

    @Override
    public SiteDTO create(Site site) {
        var rslDTO = new SiteDTO();
        if (siteRepository.existsByName(site.getName())) {
            rslDTO.setRegStatus(false);
            return rslDTO;
        }
        var password = passwordGenerator.generatePassword();
        site.setPassword(encoder.encode(password));
        site.setLogin(loginGenerator.generateLogin(site.getName()));
        siteRepository.save(site);
        rslDTO.setRegStatus(true);
        rslDTO.setGeneratedLogin(site.getLogin());
        rslDTO.setGeneratedPassword(password);
        return rslDTO;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Site> user = siteRepository.findByLogin(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }
        return new User(user.get().getLogin(), user.get().getPassword(), emptyList());
    }
}
package devPilot.backend.service;

import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

import devPilot.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import devPilot.backend.entity.User;

import java.util.Map;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
@Service 
@RequiredArgsConstructor 
public class UserService {
    private UserRepository userRepo;
    private TextEncryptor textEncryptor;

    @Transactional(readOnly=true) 
    public User requiredById(UUID id) {
        return userRepo.findById(id).orElseThrow(()->new IllegalArgumentException("No User Found with given Id"));
    }
    public String decryptAccessToken(User user){
        return textEncryptor.decrypt(user.getAccessToken());
    }
    public static Long toLong(Object value){
        if(value instanceof Number number){
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    @Transactional 
    public User upsertFromGithub(Map<String,Object> attributes, String accessToken, String scopes) {
        Long githubId = toLong(attributes.get("id"));
        String login = String.valueOf(attributes.get("login"));
        String name = attributes.get("name")!=null?
                        String.valueOf(attributes.get("name"))
                        :login;
        String avatarUrl = attributes.get("avatar_url")!=null?
                            String.valueOf(attributes.get("avatar_url"))
                            :null;
        String encryptedToken = textEncryptor.encrypt(accessToken);
        User user = userRepo.findByGithubId(githubId).orElseGet(User::new);
        user.setGithubId(githubId);
        user.setGithubUsername(login);
        user.setDisplayName(name);
        user.setAvatarUrl(avatarUrl);
        user.setAccessToken(encryptedToken);
        return userRepo.save(user);
    }
}

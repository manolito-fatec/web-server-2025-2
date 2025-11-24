package com.pardal.app.repository.logging;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.pardal.app.entity.AppUser;
import com.pardal.app.entity.log.LogEntry;
import com.pardal.app.repository.AppUserRepository;
import com.pardal.app.service.dek.DekService;
import com.pardal.app.service.vault.VaultEncryptionService;
import com.pardal.app.service.vault.VaultEncryptionService.EncryptedData;
import com.pardal.dek.entity.DataEncryptionKey;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Component
@Setter
public class MongoDbAppender extends AppenderBase<ILoggingEvent>
{
    private MongoTemplate mongoTemplate;
    private AppUserRepository userRepository;
    private DekService dekService;
    private VaultEncryptionService vaultEncryptionService;

    @Override
    protected void append(ILoggingEvent event) {
        LogEntry entry = new LogEntry();
        entry.setLevel(event.getLevel().toString());
        entry.setLogger(event.getLoggerName());
        entry.setMessage(event.getFormattedMessage());
        entry.setThread(event.getThreadName());
        entry.setTimestamp(LocalDateTime.now());
        if (event.getThrowableProxy() != null) {
            entry.setException(event.getThrowableProxy().getMessage());
        }
        Map<String, String> mdcMap = event.getMDCPropertyMap(); 

        if (mdcMap != null) {
            entry.setHttpMethod(mdcMap.get("httpMethod")); 
            entry.setRequestURI(mdcMap.get("requestURI"));
            String userEmail = mdcMap.get("userEmail");
            if(userEmail!=null)
            {
                String finalEmail = userEmail.equals("anonymousUser")
                        ? userEmail
                        : findEmail(userEmail);
                entry.setUserEmail(finalEmail);
            }
            entry.setRemoteIp(mdcMap.get("remoteIp"));
            entry.setTitle(mdcMap.get("title"));
        }
        mongoTemplate.save(entry);
    }

    public String findEmail(String email) 
    {
        Optional<AppUser> user = userRepository.findByEmailHash(email);
        Optional<DataEncryptionKey> dataEncryptionKey = dekService.findByUserId(user.get().getId());
        return vaultEncryptionService.decryptWithEnvelope
        (new EncryptedData(
                user.get().getEncryptedEmail(),
                dataEncryptionKey.get().getEmailDek()));
    }
}

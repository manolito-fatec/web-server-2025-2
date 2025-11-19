package com.pardal.app.service.terms;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.pardal.app.entity.AppUser;
import com.pardal.app.entity.dto.terms.CheckInRegisterAndUpdate;
import com.pardal.app.entity.dto.terms.NewTermDto;
import com.pardal.app.entity.dto.terms.RegisterAndUpdateCheckDto;
import com.pardal.app.entity.dto.terms.UseOfTermLoginDto;
import com.pardal.app.entity.terms.TermsCheckItems;
import com.pardal.app.entity.terms.TermsOfUse;
import com.pardal.app.entity.terms.UserTermsAcceptance;
import com.pardal.app.entity.terms.UserTermsAssignment;
import com.pardal.app.mail.EmailService;
import com.pardal.app.repository.AppUserRepository;
import com.pardal.app.repository.terms.TermsCheckItemsRepository;
import com.pardal.app.repository.terms.TermsOfUseRepository;
import com.pardal.app.repository.terms.UserTermsAcceptancedRepository;
import com.pardal.app.repository.terms.UserTermsAssignmentRepository;
import com.pardal.app.service.dek.DekService;
import com.pardal.app.service.vault.VaultEncryptionService;
import com.pardal.app.service.vault.VaultEncryptionService.EncryptedData;
import com.pardal.dek.entity.DataEncryptionKey;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TermsOfUseServiceImpl implements TermsOfUseService
{

    private final TermsOfUseRepository termsOfUseRepository;
    private final UserTermsAssignmentRepository userTermsAssignmentRepository;
    private final UserTermsAcceptancedRepository acceptancedRepository;
    private final AppUserRepository appUserRepository;
    private final TermsCheckItemsRepository termsCheckItemsRepository;
    private final EmailService emailService;
    private final VaultEncryptionService vaultEncryptionService;
    private final DekService dekService;

    @Override
    public void createNewTerm (NewTermDto newTerm)
    {
        validateNewTerm(newTerm);
        var term = new TermsOfUse();
        term.setTitle(newTerm.getTitle());
        term.setContent(newTerm.getContent());
        term.setCheckList(newTerm.getCheckList());
        term.setCreatedAt(LocalDateTime.now());
        termsOfUseRepository.save(term);
    }

    @Override
    public TermsOfUse getCurrentlyTerm ()
    {
        Optional<TermsOfUse> term = termsOfUseRepository.findTopByOrderByCreatedAtDesc();
        if(term.isEmpty())
        {
            log.error("Termo de uso não encontrado");
            throw new NoSuchElementException("term not foud");
        }
        return term.get();
    }

   private void validateNewTerm(NewTermDto newTerm)
   {
        if (newTerm == null) {
            log.error("Tentativa de criar um novo termo com objeto DTO nulo.");
            throw new IllegalArgumentException("NewTerm object cannot be null.");
        }


        if (newTerm.getTitle() == null || newTerm.getTitle().trim().isEmpty()) {
            log.error("Tentativa de criar termo falhou: Título é nulo ou vazio.");
            throw new IllegalArgumentException("Term title cannot be null or empty.");
        }


        if (newTerm.getContent() == null || newTerm.getContent().trim().isEmpty()) {
            log.error("Tentativa de criar termo falhou: Conteúdo é nulo ou vazio.");
            throw new IllegalArgumentException("Term content cannot be null or empty.");
        }


        if (newTerm.getCheckList() == null || newTerm.getCheckList().isEmpty()) {
            log.error("Tentativa de criar termo falhou: CheckList é nula ou vazia.");
            throw new IllegalArgumentException("Term check list cannot be null or empty.");
        }
    }

   @Override
   public UseOfTermLoginDto contractIsActive ( Integer userId )
   {
       Optional<UserTermsAssignment> assigened = userTermsAssignmentRepository.findByUserIdAndMostRecentTerm(userId);
       if(assigened.isEmpty())
       {
           log.error("Termo de uso não encontrado");
           throw new NoSuchElementException("term not foud");
       }
       var isActive = assigened.get().isPending();

       TermsOfUse term = null;

       if(isActive)
       {
           term = assigened.get().getTerms();
       }
       
       return new UseOfTermLoginDto(isActive, term);
   }

   @Override
   public void updateContract( RegisterAndUpdateCheckDto register )
   {
       assignTermPending(register.getTermAccepted(), register.getUserId(), register.getTermsId());
       List<UserTermsAcceptance> listOfCheck = acceptancedRepository
               .findByUserIdUserIdAndTermsTermsId(register.getUserId(), register.getTermsId());

       Map<Integer, UserTermsAcceptance> acceptanceMap = listOfCheck.stream()
               .collect(Collectors.toMap(
                       acceptance -> acceptance.getCheck().getCheckId(),
                       acceptance -> acceptance
                       ));

       for (CheckInRegisterAndUpdate checkUpdate : register.getCheckList())
       {

           UserTermsAcceptance acceptance = acceptanceMap.get(checkUpdate.checkId());

           if (acceptance != null)
           {
               acceptance.setAccepted(checkUpdate.check());
           }
       }
       acceptancedRepository.saveAll(listOfCheck);
   }


   private void assignTermPending(Boolean validated, Integer userId, Integer termId)
   {
       termNotPending(validated);
       UserTermsAssignment assignment = userTermsAssignmentRepository
           .findByUserIdAndMostRecentTerm(userId)
           .orElseThrow(() -> new RuntimeException(
               "Term assignment not found for user ID: " + userId
           ));
       assignment.setPending(false);
       userTermsAssignmentRepository.save(assignment);
   }

   public void termNotPending(Boolean validated) {
       if (!validated) {
           throw new IllegalStateException("Terms of use are mandatory");
       }
   }

   private UserTermsAssignment assignTermToUser(AppUser userId, TermsOfUse termId)
   {
       UserTermsAssignment assignment = new UserTermsAssignment();
       assignment.setUserId(userId);
       assignment.setTerms(termId);
       assignment.setPending(false);
       return userTermsAssignmentRepository.save(assignment);
   }

   @Override
   public void registerContract ( RegisterAndUpdateCheckDto register )
   {
       AppUser user = appUserRepository.findById(register.getUserId())
               .orElseThrow(() -> new RuntimeException("User not found with ID: " + register.getUserId()));

       TermsOfUse term = termsOfUseRepository.findById(register.getTermsId())
               .orElseThrow(() -> new RuntimeException("Term of Use not found with ID: " + register.getTermsId()));

       assignTermToUser(user, term);

       List<Integer> checkIds = register.getCheckList().stream()
                                     .map(CheckInRegisterAndUpdate::checkId)
                                     .collect(Collectors.toList());

       List<TermsCheckItems> realChecks = termsCheckItemsRepository.findAllById(checkIds);

       Map<Integer, TermsCheckItems> checkMap = realChecks.stream()
           .collect(Collectors.toMap(TermsCheckItems::getCheckId, item -> item));

       List<UserTermsAcceptance> contractsToSave = new ArrayList<>();

       for (CheckInRegisterAndUpdate checkUpdate : register.getCheckList()) {

           TermsCheckItems realCheck = checkMap.get(checkUpdate.checkId());

           if (realCheck == null) {
               throw new RuntimeException("Check item not found with ID: " + checkUpdate.checkId());
           }

           if(checkUpdate.label().toUpperCase().contains("EMAIL") && checkUpdate.check())
           {
               Optional<DataEncryptionKey> deks = dekService.findByUserId(user.getId());
               String email = vaultEncryptionService.decryptWithEnvelope(new EncryptedData(
                       user.getEncryptedEmail(), deks.get().getEmailDek()));
               String userName = vaultEncryptionService.decryptWithEnvelope(new EncryptedData(
                       user.getEncryptedName(),deks.get().getNameDek()));
               emailService.sendEmailUseTerm(email, userName);
           }
           UserTermsAcceptance contract = new UserTermsAcceptance();
           contract.setCheck(realCheck);
           contract.setAccepted(checkUpdate.check());
           contract.setTerms(term);
           contract.setUserId(user);

           contractsToSave.add(contract);
       }

       acceptancedRepository.saveAll(contractsToSave);
   }

}

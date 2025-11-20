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
import com.pardal.app.entity.dto.terms.CheckInRegisterAndUpdateDto;
import com.pardal.app.entity.dto.terms.CheckResponseDto;
import com.pardal.app.entity.dto.terms.NewTermDto;
import com.pardal.app.entity.dto.terms.RegisterAndUpdateCheckDto;
import com.pardal.app.entity.dto.terms.TermByCustomerDto;
import com.pardal.app.entity.dto.terms.TermOfUseDto;
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
import com.pardal.app.service.appUser.AppUserService;

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
    private final AppUserService userService;

    /**
     * Creates and saves a new Terms of Use entity based on the provided DTO.
     * This method first validates the input and then maps the data from the DTO
     * to the TermsOfUse entity before persisting it to the database.
     * * @param newTerm The Data Transfer Object (DTO) containing the title, content, and checklist 
     * for the new Terms of Use.
     * @throws InvalidTermDataException if the validation of the new term fails (inferred from validateNewTerm).
     * @author paulo arantes
     */
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

    /**
     * Retrieves the most recently created Terms of Use document.
     * It is assumed that the term with the latest creation timestamp is the currently active term.
     *
     * @return The most recent {@link TermsOfUse} object.
     * @throws NoSuchElementException If no Terms of Use document is found in the repository.
     * @author paulo arantes
     */
    @Override
    public TermsOfUse getCurrentlyTerm() {
        return termsOfUseRepository.findTopByOrderByCreatedAtDesc()
                .orElseThrow(() -> {
                    log.error("Termo de uso não encontrado");
                    return new NoSuchElementException("term not found");
                });
    }

    /**
     * Validates the data contained within the {@link NewTermDto} before creating a new Terms of Use entity.
     * Checks for nullity and empty values in essential fields (title, content, and checklist).
     *
     * @param newTerm The DTO containing the data for the new Terms of Use.
     * @throws IllegalArgumentException If the DTO itself is null, or if the title, content, or check list
     * fields are null or empty
     * @author paulo arantes
     */
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

    /**
     * Checks if the user has an active (pending) Terms of Use contract assignment.
     * It retrieves the most recent term assigned to the user and verifies its pending status.
     *
     * @param userId The unique identifier of the user to check.
     * @return A {@link UseOfTermLoginDto} containing the activation status (isActive), 
     * and the term details and checklist if the contract is pending (isActive = true).
     * @throws NoSuchElementException If no Terms of Use assignment is found for the given user ID.
     * @author paulo arantes
     */
   @Override
   public UseOfTermLoginDto contractIsActive ( Integer userId )
   {
       Optional<UserTermsAssignment> assigened = userTermsAssignmentRepository.findByUserIdAndMostRecentTerm(userId);
       if(assigened.isEmpty())
       {
           log.error("Termo de uso não encontrado");
           throw new NoSuchElementException("term not found");
       }
       var isActive = assigened.get().isPending();
       TermsOfUse term = null;

       if(isActive)
       {
           term = assigened.get().getTerms();
           var termResponse = new TermOfUseDto(term.getTermsId(), term.getTitle(), term.getContent());

           List<CheckResponseDto> checks = term.getCheckList().stream()
                   .map(a -> new CheckResponseDto(
                           a.getCheckId(),
                           a.getLabel(),
                           a.getRequired(),
                           Boolean.FALSE
                           ))
                   .collect(java.util.stream.Collectors.toList());
           return new UseOfTermLoginDto(isActive, termResponse, checks);
       }
       return new UseOfTermLoginDto(isActive, null, null);
   }

   /**
    * Checks if the user has an active (pending) Terms of Use contract assignment.
    * It retrieves the most recent term assigned to the user and checks its pending status.
    *
    * @param userId The unique identifier of the user to check.
    * @return A {@link UseOfTermLoginDto} containing the activation status (isActive), 
    * and the term details and checklist if the contract is pending (isActive = true).
    * @throws NoSuchElementException If no Terms of Use assignment is found for the given user ID.
    * @author paulo arantes
    */
   @Override
   public void updateContract( RegisterAndUpdateCheckDto register )
   {
       assignTermPending(register.getTermAccepted(), register.getUserId(), register.getTermsId());
       List<UserTermsAcceptance> listOfCheck = acceptancedRepository
               .findByUserIdAndTermsId(register.getUserId(), register.getTermsId());

       Map<Integer, UserTermsAcceptance> acceptanceMap = listOfCheck.stream()
               .collect(Collectors.toMap(
                       acceptance -> acceptance.getCheck().getCheckId(),
                       acceptance -> acceptance
                       ));

       for (CheckResponseDto checkUpdate : register.getCheckList())
       {

           UserTermsAcceptance acceptance = acceptanceMap.get(checkUpdate.getCheckId());

           if (acceptance != null)
           {
               acceptance.setAccepted(checkUpdate.getCheck());
           }

           if(checkUpdate.getLabel().toUpperCase().contains("EMAIL") && checkUpdate.getCheck())
           {
               sendTermUseEmail(register.getUserId());
           }
       }
       acceptancedRepository.saveAll(listOfCheck);
   }

   /**
    * Updates the pending status of a user's most recent Terms of Use assignment to 'false',
    * effectively marking the term as accepted or no longer pending.
    * This operation is executed only after external validation confirms the assignment should be finalized.
    *
    * @param validated A flag indicating whether the external validation check was successful. 
    * This is used internally by {@code termNotPending}.
    * @param userId    The unique identifier of the user whose term assignment is being updated.
    * @param termId    The ID of the Terms of Use document (although not explicitly used here, it provides context).
    * @throws RuntimeException If the most recent {@link UserTermsAssignment} for the given user ID is not found.
    * @author paulo arantes
    */
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

   /**
    * Ensures that the external validation status of a Terms of Use assignment is true.
    * This method is typically used to prevent further execution if the mandatory terms
    * have not been validated (i.e., accepted).
    *
    * @param validated A flag indicating the result of the external validation check (e.g., whether the user
    * has successfully accepted the mandatory terms).
    * @throws IllegalStateException If the validation flag is false, indicating that the mandatory
    * Terms of Use were not validated
    * @author paulo arantes
    */
   public void termNotPending(Boolean validated) {
       if (!validated) {
           throw new IllegalStateException("Terms of use are mandatory");
       }
   }

   /**
    * Creates and saves a new assignment record, linking a specific user to a Terms of Use document.
    * The assignment is initially marked as non-pending (accepted or finalized) when created.
    *
    * @param userId The {@link AppUser} entity representing the user to whom the term is assigned.
    * @param termId The {@link TermsOfUse} entity being assigned to the user
    * @author paulo arantes
    * @return The saved {@link UserTermsAssignment} entity with the user and term linkage.
    */
   private UserTermsAssignment assignTermToUser(AppUser userId, TermsOfUse termId)
   {
       UserTermsAssignment assignment = new UserTermsAssignment();
       assignment.setUserId(userId);
       assignment.setTerms(termId);
       assignment.setPending(false);
       return userTermsAssignmentRepository.save(assignment);
   }

   /**
    * Registers the user's acceptance status for all items listed in a specific Terms of Use document.
    * This method performs several steps:
    * 1. Retrieves and validates the user and the Terms of Use entities.
    * 2. Assigns the term to the user.
    * 3. Validates the provided checklist items against the actual database checks.
    * 4. Processes the checklist, potentially sending an email if an 'EMAIL' check is accepted.
    * 5. Saves all individual acceptance statuses (contracts) to the repository.
    *
    * @param register The {@link RegisterAndUpdateCheckDto} containing the user ID, terms ID, 
    * and the list of checks with their accepted status.
    * @throws RuntimeException If the user ID or the Terms of Use ID is not found.
    * @throws RuntimeException If any check item ID provided in the checklist is not found in the database.
    * @author paulo arantes
    */
   @Override
   public void registerContract ( RegisterAndUpdateCheckDto register )
   {
       AppUser user = appUserRepository.findById(register.getUserId())
               .orElseThrow(() -> new RuntimeException("User not found with ID: " + register.getUserId()));

       TermsOfUse term = termsOfUseRepository.findById(register.getTermsId())
               .orElseThrow(() -> new RuntimeException("Term of Use not found with ID: " + register.getTermsId()));

       assignTermToUser(user, term);

       List<Integer> checkIds = register.getCheckList().stream()
                                     .map(CheckInRegisterAndUpdateDto::getCheckId)
                                     .collect(Collectors.toList());

       List<TermsCheckItems> realChecks = termsCheckItemsRepository.findAllById(checkIds);

       Map<Integer, TermsCheckItems> checkMap = realChecks.stream()
           .collect(Collectors.toMap(TermsCheckItems::getCheckId, item -> item));

       List<UserTermsAcceptance> contractsToSave = new ArrayList<>();

       for (CheckResponseDto checkUpdate : register.getCheckList()) {

           TermsCheckItems realCheck = checkMap.get(checkUpdate.getCheckId());

           if (realCheck == null) {
               throw new RuntimeException("Check item not found with ID: " + checkUpdate.getCheckId());
           }

           if(checkUpdate.getLabel().toUpperCase().contains("EMAIL") && checkUpdate.getCheck())
           {
               sendTermUseEmail(user.getId());
           }
           UserTermsAcceptance contract = new UserTermsAcceptance();
           contract.setCheck(realCheck);
           contract.setAccepted(checkUpdate.getCheck());
           contract.setTerms(term);
           contract.setUserId(user);

           contractsToSave.add(contract);
       }

       acceptancedRepository.saveAll(contractsToSave);
   }

   /**
    * Retrieves the complete details of the most recent Terms of Use accepted by a specific user, 
    * including the acceptance status for each individual check item.
    * * The term details (ID, title, content) are derived from the first acceptance record found, 
    * which corresponds to the most recent term ID for that user.
    * * @param userId The unique identifier of the user whose accepted term details are to be retrieved.
    * @return A {@link TermByCustomerDto} containing the details of the most recent accepted term, 
    * the user ID, and a list of checks with their acceptance status ({@code Boolean.TRUE} or {@code Boolean.FALSE}).
    * Returns an empty DTO if no acceptance history is found for the user.
    * @throws RuntimeException If a database error occurs while attempting to fetch the acceptance history.
    * @author paulo arantes
    */
   @Override
   public TermByCustomerDto getTermByUser ( Integer userId )
   {
       List<UserTermsAcceptance> listOfAcceptances = new ArrayList<UserTermsAcceptance>();
       try {
           listOfAcceptances = acceptancedRepository.findByUserIdAndMaxTermsId(userId);
       }catch (Exception e) {
           log.error("Erro ao buscar lista de histórico de aceitações para o userId: {}", userId);
           throw new RuntimeException("Failed to retrieve user terms acceptance history for user" + userId, e);
       }

       TermByCustomerDto response = new TermByCustomerDto();

       if (listOfAcceptances.isEmpty())
       {
           return response; 
       }

       UserTermsAcceptance firstAcceptance = listOfAcceptances.get(0);
       Integer termsId = firstAcceptance.getTerms().getTermsId();
       String title = firstAcceptance.getTerms().getTitle();
       String content = firstAcceptance.getTerms().getContent();
       List<CheckResponseDto> checks = listOfAcceptances.stream()
               .map(a -> new CheckResponseDto(
                       a.getCheck().getCheckId(),
                       a.getCheck().getLabel(),
                       a.getCheck().getRequired(),
                       a.getAccepted()
                       ))
               .collect(java.util.stream.Collectors.toList());
       response.setTerm(new TermOfUseDto(termsId, title, content));
       response.setUserId(firstAcceptance.getUserId() != null ? firstAcceptance.getUserId().getId() : null); 
       response.setChecks(checks);
       return response;
   }

   /**
    * Retrieves user details and sends a notification email confirming the acceptance or registration
    * of the Terms of Use.
    *
    * @param userId The unique identifier of the user who accepted the terms.
    * @author paulo arantes
    */
   private void sendTermUseEmail(Integer userId)
   {
       var user = userService.getUserById(userId);
       emailService.sendEmailUseTerm(user.getEmail(), user.getName());
   }

}

package com.pardal.app.entity.terms;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "terms_of_use", schema = "pardal")
public class TermsOfUse
{
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Integer termsId;

     private String title;
     private String content;
     private LocalDateTime createdAt;

     @OneToMany(mappedBy = "termsId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
     @JsonManagedReference
     private List<TermsCheckItems> checkList;

     public void setCheckList(List<TermsCheckItems> checkList)
     {
        this.checkList = checkList;

        if(!hasRequiredItem(checkList)) {
            throw new RuntimeException("At least one mandatory check is required.");
        }

        if (checkList != null && hasRequiredItem(checkList)) {
             checkList.forEach(
                     item -> item.setTermsId(this));
        }
     }

     private boolean hasRequiredItem(List<TermsCheckItems> checkList)
     {
         return checkList != null &&
                checkList.stream().anyMatch(TermsCheckItems::getRequired);
     }
}

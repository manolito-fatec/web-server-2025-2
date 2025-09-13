"""
Created on 09/10/2025 00:41 

@author: otavio-calderan
"""

import logging
import re

import pt_core_news_lg

log = logging.getLogger(__name__)


class Anonymizer:
    """Logic for finding and masking personal data."""
    ANONYMIZATION_MASK = "[DADO PESSOAL ANONIMIZADO]"

    _CPF_REGEX = re.compile(r'\d{3}\.?\d{3}\.?\d{3}-?\d{2}')
    _EMAIL_REGEX = re.compile(r'[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}')
    _PHONE_REGEX = re.compile(
        r'(\(?\+?[0-9]{1,3}\)?\s?)?\(?[0-9]{2,4}\)?\s?([0-9]{2,5})?[-.\s]?([0-9]{3,5})[-.\s]?([0-9]{3,5})')
    _REGEX_PATTERNS = [_CPF_REGEX, _EMAIL_REGEX, _PHONE_REGEX]

    try:
        _NLP_MODEL = pt_core_news_lg.load()
    except OSError:
        log.error("ERROR: SpaCy model 'pt_core_news_lg' not found.")
        _NLP_MODEL = None

    def anonymize_with_ner(self, text: str) -> tuple[str, bool]:
        """Applies the NER model to find and mask people's names."""
        if not isinstance(text, str) or not text or not self._NLP_MODEL:
            return text, False

        doc = self._NLP_MODEL(text)
        new_text = text
        found_pii = False

        for ent in reversed(doc.ents):
            if ent.label_ == "PER":
                start, end = ent.start_char, ent.end_char
                new_text = new_text[:start] + self.ANONYMIZATION_MASK + new_text[end:]
                found_pii = True

        return new_text, found_pii

    def anonymize_with_regex(self, text: str) -> tuple[str, bool]:
        """Applies regex patterns to find and mask personal data in a text."""
        if not isinstance(text, str) or not text:
            return text, False

        anonymized_text = text
        found_pii = False

        for pattern in self._REGEX_PATTERNS:
            def repl(match):
                nonlocal found_pii
                found_pii = True
                return self.ANONYMIZATION_MASK

            anonymized_text = pattern.sub(repl, anonymized_text)

        return anonymized_text, found_pii
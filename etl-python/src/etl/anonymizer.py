"""
Created on 09/10/2025 00:41 

@author: otavio-calderan
"""

import logging
import re

import spacy

log = logging.getLogger(__name__)


class Anonymizer:
    """Logic for finding and masking personal data."""
    ANONYMIZATION_MASK = "[DADO PESSOAL ANONIMIZADO]"

    _CPF_REGEX = re.compile(r'\d{3}\.?\d{3}\.?\d{3}-?\d{2}')
    _EMAIL_REGEX = re.compile(r'[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}')
    _PHONE_REGEX = re.compile(
        r'(\(?\+?[0-9]{1,3}\)?\s?)?\(?[0-9]{2,4}\)?\s?([0-9]{2,5})?[-.\s]?([0-9]{3,5})[-.\s]?([0-9]{3,5})')
    _REGEX_PATTERNS = [_CPF_REGEX, _EMAIL_REGEX, _PHONE_REGEX]

    _NLP_MODEL = None

    try:
        spacy.require_gpu()
        log.info("GPU activated.")

        _NLP_MODEL = spacy.load("pt_core_news_lg")
        log.info("pt_core_news_lg' model loaded on GPU.")

    except Exception as e:
        log.warning(f"GPU not activated or model failed to load. Using CPU. Error: {e}")
        if not _NLP_MODEL:
            try:
                _NLP_MODEL = spacy.load("pt_core_news_lg")
                log.info("pt_core_news_lg' model loaded on CPU.")
            except OSError:
                log.error("SpaCy model 'pt_core_news_lg' not found.")

    def anonymize_with_ner(self, texts: list[str], batch_size: int = 3000) -> list[tuple[str, bool]]:
        """Applies the NER model to find and mask people's names."""
        if not texts or not self._NLP_MODEL:
            return [(text, False) for text in texts]

        results = []
        docs = self._NLP_MODEL.pipe(texts, batch_size=batch_size)

        for original_text, doc in zip(texts, docs):
            new_text = original_text
            found_pii = False
            for ent in reversed(doc.ents):
                if ent.label_ == "PER":
                    start, end = ent.start_char, ent.end_char
                    new_text = new_text[:start] + self.ANONYMIZATION_MASK + new_text[end:]
                    found_pii = True
            results.append((new_text, found_pii))

        return results

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
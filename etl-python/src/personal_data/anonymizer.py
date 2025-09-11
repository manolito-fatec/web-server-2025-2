"""
Created on 09/10/2025 00:41 

@author: otavio-calderan
"""

import re

ANONYMIZATION_MASK = "[DADO PESSOAL ANONIMIZADO]"

CPF_REGEX = re.compile(r'\d{3}\.?\d{3}\.?\d{3}-?\d{2}')
EMAIL_REGEX = re.compile(r'[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}')
PHONE_REGEX = re.compile(
    r'(\(?\+?[0-9]{1,3}\)?\s?)?\(?[0-9]{2,4}\)?\s?([0-9]{2,5})?[-.\s]?([0-9]{3,5})[-.\s]?([0-9]{3,5})')
PATTERNS = [CPF_REGEX, EMAIL_REGEX, PHONE_REGEX]


def anonymize_text(text: str) -> tuple[str, bool]:
    """ Aplica padrões de regex para encontrar e mascarar dados pessoais em um texto. """
    if not isinstance(text, str) or not text:
        return text, False

    anonymized_text = text
    found_pii = False

    for pattern in PATTERNS:
        def repl(match):
            nonlocal found_pii
            found_pii = True
            return ANONYMIZATION_MASK

        anonymized_text = pattern.sub(repl, anonymized_text)

    return anonymized_text, found_pii

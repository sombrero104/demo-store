package com.store.exception

class AccountAlreadyExistsException(email: String) :
    RuntimeException("Email is already in use: $email")

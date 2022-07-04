package com.example.demo.contract

import com.wavesenterprise.sdk.contract.api.annotation.ContractAction
import com.wavesenterprise.sdk.contract.api.annotation.ContractHandler
import com.wavesenterprise.sdk.contract.api.annotation.ContractInit
import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping
import com.wavesenterprise.sdk.node.domain.contract.ContractTransaction
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@ContractHandler
class PersonContractHandler(
    private val contractState: ContractState,
    private val contractTransaction: ContractTransaction,
) {
    private val personBySnils: Mapping<Person> = contractState.getMapping(Person::class.java, "PERSON_SNILS")

    @ContractInit
    fun createContract() {
        contractState.put("CREATOR", "Created by ${contractTransaction.sender} at " +
                txLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    @ContractAction
    fun registerPerson(personRequest: PersonRequest) {
        if (personBySnils.has(personRequest.snils)) {
            throw IllegalArgumentException("Snils ${personRequest.snils} has already been taken")
        }
        personBySnils.put(personRequest.snils, personRequest.toDto())
    }

    private fun txLocalDate() = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(contractTransaction.timestamp.utcTimestampMillis),
        ZoneId.of("UTC")
    )
}

private fun PersonRequest.toDto(): Person = Person(
    name = name,
    age = age,
    snils = snils
)

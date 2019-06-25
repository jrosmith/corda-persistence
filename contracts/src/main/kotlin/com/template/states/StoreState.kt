package com.template.states

import com.template.contracts.StoreContract
import com.template.schema.StoreSchemaV1
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import java.lang.IllegalArgumentException

// *********
// * State *
// *********
@BelongsToContract(StoreContract::class)
data class StoreState(
        val storeName: String,
        val productNames: MutableList<String>,
        override val participants: List<AbstractParty> = listOf(),
        override var linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState, QueryableState {

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is StoreSchemaV1 -> StoreSchemaV1.mapState(this)
            else -> throw IllegalArgumentException("Unrecognized schema: $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(StoreSchemaV1)
}

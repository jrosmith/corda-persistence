package com.template.schema

import com.template.states.StoreState
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.serialization.CordaSerializable
import java.util.*
import javax.persistence.*

object StoreSchema

@CordaSerializable
object StoreSchemaV1 : MappedSchema(schemaFamily = StoreSchema::class.java, version = 1, mappedTypes = listOf(PersistentParentStore::class.java, PersistentChildProduct::class.java)) {

    fun mapState(storeState: StoreState): PersistentState {
        val persistentChildProducts = storeState.productNames.map { productName ->
            PersistentChildProduct(
                    productName = productName,
                    persistentParentStore = storeState
            )
        }.toMutableList()

        return PersistentParentStore(
                storeName = storeState.storeName,
                linear_id = storeState.linearId.id,
                listOfPersistentChildren = persistentChildProducts
        )
    }

    @Entity
    @Table(name = "parent_data")
    class PersistentParentStore(
            @Column(name = "store_name")
            var storeName: String,

            @Column(name = "linear_id")
            var linear_id: UUID,


            @OneToMany(
                    //
                    // *** Comment 3 ***
                    //
                    // required to fix the following intermediate error:
                    // org.hibernate.TransientObjectException: object references an unsaved transient instance - save the
                    // transient instance before flushing: com.template.schema.StoreSchemaV1$PersistentChildProduct
                    // java.lang.IllegalStateException: org.hibernate.TransientObjectException: object references an
                    // unsaved transient instance - save the transient instance before
                    // flushing: com.template.schema.StoreSchemaV1$PersistentChildProduct
                    cascade = [CascadeType.ALL],

                    //
                    // *** Comment 4 ***
                    //
                    // required to fix the following intermediate error:
                    // Could not create Hibernate configuration: Could not determine type for: java.util.List, at
                    // table: parent_data, for columns: [org.hibernate.mapping.Column(listOfPersistentChildren)]
                    // net.corda.nodeapi.internal.persistence.HibernateConfigException: Could not create Hibernate
                    // configuration: Could not determine type for: java.util.List, at table: parent_data, for
                    // columns: [org.hibernate.mapping.Column(listOfPersistentChildren)]
                    targetEntity = PersistentChildProduct::class
            )
            @JoinColumns(
                    JoinColumn(name = "transaction_id", referencedColumnName = "transaction_id"),
                    JoinColumn(name = "output_index", referencedColumnName = "output_index")
            )
            var listOfPersistentChildren: MutableList<PersistentChildProduct>
    ) : PersistentState()

    @Entity
    @CordaSerializable
    @Table(name = "child_data")
    class PersistentChildProduct(
            //
            // *** Comment 1 ***
            //
            // including the @Id annotations following generates the following error:
            // Could not create Hibernate configuration: net.corda.core.schemas.PersistentStateRef must not have @Id
            // properties when used as an @EmbeddedId: com.template.schema.StoreSchemaV1$PersistentChildProduct.stateRef
            // net.corda.nodeapi.internal.persistence.HibernateConfigException: Could not create Hibernate
            // configuration: net.corda.core.schemas.PersistentStateRef must not have @Id properties when used as an
            // @EmbeddedId: com.template.schema.StoreSchemaV1$PersistentChildProduct.stateRef
            //
            //
            // *** Comment 2 ***
            //
            // commenting out the @Id annotation generates further annotation related error conditions. these errors
            // are resolved by including the @OneToMany tag in the parent with the fields referenced in comments 3 and 4.
            //
            // commenting out the @Id annotation and including the fields referenced in Comments 3 and 4 create the
            // following error condition:
            // org.hibernate.id.IdentifierGenerationException: null id generated
            // for:class com.template.schema.StoreSchemaV1$PersistentChildProduct javax.persistence.PersistenceException:
            // org.hibernate.id.IdentifierGenerationException: null id generated
            // for:class com.template.schema.StoreSchemaV1$PersistentChildProduct
            //
            // *** Summary ***
            // the class [PersistentState] that the parent and child implements is as follows:
            //
            // ```
            // @KeepForDJVM
            // @MappedSuperclass
            // @CordaSerializable
            // class PersistentState(@EmbeddedId override var stateRef: PersistentStateRef? = null) : DirectStatePersistable
            // ```
            //
            // as you can see, the @EmbeddedId annotation referenced in Comment 1 prevents the use of the @Id annotation
            // below, but the error in Comment 2 seems to suggest that the @Id annotation is required in order to properly
            // create the one to many relationship.
            @Id
            var Id: UUID = UUID.randomUUID(),

            @Column(name = "product_name")
            var productName: String,

            @ManyToOne(targetEntity = PersistentParentStore::class)
            var persistentParentStore: StoreState
    ) : PersistentState()
}
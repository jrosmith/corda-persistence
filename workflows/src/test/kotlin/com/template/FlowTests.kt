package com.template

import com.template.flows.Initiator
import com.template.flows.Responder
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Before
import org.junit.Test

class FlowTests {
    private lateinit var network: MockNetwork
    private lateinit var a: StartedMockNode
    private lateinit var b: StartedMockNode



    @Before
    fun setup() {
        val network = MockNetwork(
                MockNetworkParameters(
                        threadPerNode = true,
                        cordappsForAllNodes = listOf(
                                TestCordapp.findCordapp("com.template.contracts"),
                                TestCordapp.findCordapp("com.template.flows")
                        )
                )
        )

        a = network.createNode()
        b = network.createNode()

        listOf(a, b).forEach { it.registerInitiatedFlow(Responder::class.java) }
    }

    @After
    fun tearDown() = network.stopNodes()

    @Test
    fun `dummy test`() {
        val saveStoreFlow= Initiator(
                storeName = "groceryStore1",
                productNames = mutableListOf("apples", "oranges", "bananas"),
                recipients = listOf(b.info.legalIdentities.first())
        )

        val saveStoreFuture = a.startFlow(saveStoreFlow)

        saveStoreFuture.getOrThrow()
    }
}
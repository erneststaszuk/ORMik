package com.example.ormik.shop

import com.example.ormik.events.DomainEvent
import com.example.ormik.policy.PolicyAlreadyIsPaidFurther
import com.example.ormik.policy.PolicyParties
import com.example.ormik.policy.SelectedRisk
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.Embedded.OnEmpty
import org.springframework.data.relational.core.mapping.MappedCollection
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Table
data class Cart(
    @Id val id: UUID,
    val promoCode: String,
    @MappedCollection(idColumn = "cart_id", keyColumn = "seq_order")
    val merchantCarts: List<MerchantCart>,
    @Version val version: Long = 0L,
    @Transient val events: Set<DomainEvent>
)

@Table
data class MerchantCart(
    @Id val id: UUID,
    @MappedCollection(idColumn = "merchant_cart_id")
    val items: List<Item>,
    @Embedded(onEmpty = OnEmpty.USE_NULL)
    val selectedDeliveryMethod: DeliveryMethod?,
)

@Table
data class Item(
    val merchantCartId: UUID,
    @Id val id: Long,
    val cataloguePrice: BigDecimal,
    val finalPrice: BigDecimal,
)

data class DeliveryMethod(
    val name: String,
    val price: BigDecimal
)

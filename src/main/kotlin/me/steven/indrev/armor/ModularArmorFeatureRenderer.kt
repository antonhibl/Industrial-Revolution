package me.steven.indrev.armor

import me.steven.indrev.items.armor.IRModularArmor
import me.steven.indrev.utils.identifier
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.BipedEntityModel
import net.minecraft.client.render.item.ItemRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ArmorItem
import net.minecraft.util.Identifier

class ModularArmorFeatureRenderer<T : LivingEntity, M : BipedEntityModel<T>, A : BipedEntityModel<T>>(
    context: FeatureRendererContext<T, M>,
    private val leggingsModel: A,
    private val bodyModel: A
) : ArmorFeatureRenderer<T, M, A>(context, leggingsModel, bodyModel) {

    override fun render(matrixStack: MatrixStack, vertexConsumerProvider: VertexConsumerProvider, i: Int, livingEntity: T, f: Float, g: Float, h: Float, j: Float, k: Float, l: Float) {
        renderArmor(matrixStack, vertexConsumerProvider, livingEntity, EquipmentSlot.CHEST, i, getArmor(EquipmentSlot.CHEST))
        renderArmor(matrixStack, vertexConsumerProvider, livingEntity, EquipmentSlot.LEGS, i, getArmor(EquipmentSlot.LEGS))
        renderArmor(matrixStack, vertexConsumerProvider, livingEntity, EquipmentSlot.FEET, i, getArmor(EquipmentSlot.FEET))
        renderArmor(matrixStack, vertexConsumerProvider, livingEntity, EquipmentSlot.HEAD, i, getArmor(EquipmentSlot.HEAD))
    }

    private fun renderArmor(matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, livingEntity: T, equipmentSlot: EquipmentSlot, i: Int, bipedEntityModel: A) {
        val itemStack = livingEntity.getEquippedStack(equipmentSlot)
        val item = itemStack.item
        if (item is IRModularArmor && item.material == IRArmorMaterial.MODULAR && item.slotType == equipmentSlot) {
            (this.contextModel as BipedEntityModel<T>).setAttributes(bipedEntityModel)
            setVisible(bipedEntityModel, equipmentSlot)
            val bl = usesSecondLayer(equipmentSlot)
            val bl2 = itemStack.hasGlint()
            // base will be done by the default armor renderer
            // renderArmorParts(matrices, vertexConsumers, i, item, bl2, bipedEntityModel, bl, 1.0f, 1.0f, 1.0f, null)
            val rgb = item.getColor(itemStack)
            val r = (rgb and 0xFF0000 shr 16) / 255f
            val g = (rgb and 0xFF00 shr 8) / 255f
            val b = (rgb and 0xFF) / 255f
            Module.getInstalled(itemStack).filter { it.slots.contains(equipmentSlot) }.forEach { module ->
                if (module != Module.COLOR) {
                    renderArmorParts(
                        matrices, vertexConsumers, i, item, bl2, bipedEntityModel, bl, r, g, b, module.key
                    )
                }
            }
        }
    }


    private fun renderArmorParts(matrixStack: MatrixStack, vertexConsumerProvider: VertexConsumerProvider, i: Int, armorItem: ArmorItem, bl: Boolean, bipedEntityModel: A, bl2: Boolean, r: Float, g: Float, b: Float, string: String?) {
        val vertexConsumer = ItemRenderer.method_27952(vertexConsumerProvider, RenderLayer.getArmorCutoutNoCull(getArmorTexture(armorItem, bl2, string)), false, bl)
        bipedEntityModel.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, r, g, b, 1.0f)
    }

    private fun getArmor(slot: EquipmentSlot): A {
        return if (usesSecondLayer(slot)) leggingsModel else bodyModel
    }

    private fun usesSecondLayer(slot: EquipmentSlot): Boolean {
        return slot == EquipmentSlot.LEGS
    }

    private fun getArmorTexture(armorItem: ArmorItem, bl: Boolean, string: String?): Identifier? {
        val string2 = "textures/models/armor/" + armorItem.material.name + "_layer_" + (if (bl) 2 else 1) + (if (string == null) "" else "_$string") + ".png"
        return MODULAR_ARMOR_TEXTURE_CACHE.computeIfAbsent(string2) { id -> identifier(id) }
    }

    companion object {
        private val MODULAR_ARMOR_TEXTURE_CACHE = mutableMapOf<String, Identifier>()
    }
}
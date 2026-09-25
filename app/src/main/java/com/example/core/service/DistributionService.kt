package com.example.core.service

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.core.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

object DistributionService {
    suspend fun image(context: Context, product: Product): Uri = withContext(Dispatchers.IO) {
        val destination = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File(destination, "share-${product.id}.jpg")
        val local = product.localImageUri
        val data = if (local != null) {
            val input = if (local.startsWith("content:") || local.startsWith("file:")) context.contentResolver.openInputStream(Uri.parse(local)) else File(local).inputStream()
            requireNotNull(input).use { it.readBytesBounded() }
        } else {
            val url = requireNotNull(product.imageUrl ?: product.originalImageUrl)
            require(url.startsWith("https://"))
            OkHttpClient.Builder().callTimeout(25, TimeUnit.SECONDS).build().newCall(Request.Builder().url(url).build()).execute().use { response ->
                check(response.isSuccessful)
                requireNotNull(response.body).byteStream().use { it.readBytesBounded() }
            }
        }
        val options = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        android.graphics.BitmapFactory.decodeByteArray(data, 0, data.size, options)
        require(options.outWidth > 0 && options.outHeight > 0) { "Photo unavailable" }
        file.writeBytes(data)
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
    private fun java.io.InputStream.readBytesBounded(): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            val size = read(buffer); if (size < 0) break
            require(output.size() + size <= 10 * 1024 * 1024) { "Image is too large" }
            output.write(buffer, 0, size)
        }
        return output.toByteArray()
    }
    fun share(context: Context, image: Uri, text: String, whatsapp: Boolean) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"; putExtra(Intent.EXTRA_STREAM, image); putExtra(Intent.EXTRA_TEXT, text)
            clipData = ClipData.newRawUri("Product image", image)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (whatsapp) setPackage("com.whatsapp")
        }
        context.startActivity(if (whatsapp) intent else Intent.createChooser(intent, "Share product"))
    }
    fun ondcDraft(product: Product): JSONObject = JSONObject().apply {
        put("status", "export_only_requires_seller_integration")
        put("production_published", false)
        put("missing_setup", JSONArray(listOf("Seller network participant onboarding", "Domain category mapping", "Provider location and fulfillment", "Tax and legal declarations", "Signing keys and sandbox conformance")))
        val provider = JSONObject()
            .put("id", product.publicProfile.shop_name.ifBlank { product.publicProfile.display_name })
            .put("descriptor", JSONObject().put("name", product.publicProfile.shop_name.ifBlank { product.publicProfile.display_name }))
        val item = JSONObject()
            .put("id", product.id)
            .put("descriptor", JSONObject().put("name", product.title).put("long_desc", product.description)
                .put("images", JSONArray(listOfNotNull(product.imageUrl ?: product.originalImageUrl))))
            .put("source_category", product.category.displayName)
            .put("price", JSONObject().put("currency", "INR").put("value", java.math.BigDecimal.valueOf(product.finalPrice).toPlainString()))
        put("provider", provider.put("items", JSONArray().put(item)))
    }
    fun export(context: Context, product: Product) {
        val file = File(File(context.cacheDir, "images").apply { mkdirs() }, "ondc-${product.id}.json")
        file.writeText(ondcDraft(product).toString(2))
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "application/json"; putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri("ONDC catalog draft", uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }, "Export catalog draft"))
    }
}

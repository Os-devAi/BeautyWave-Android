package com.nexusdev.beautywave.view.products

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nexusdev.beautywave.R
import com.nexusdev.beautywave.databinding.ActivityDetailsBinding
import com.nexusdev.beautywave.entities.Constants
import com.nexusdev.beautywave.model.CardProdModel
import com.nexusdev.beautywave.model.ProductsModel

@Suppress("DEPRECATION")
class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding

    private var product: ProductsModel? = null
    private var status: String = "Disponible"
    private var cantidad: Int? = 0
    private val auth = FirebaseAuth.getInstance()
    private var cantidadI: Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        getData()
        click()
    }

    @SuppressLint("SetTextI18n")
    private fun getData() {
        this.product = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra("product")
        } else {
            intent.getParcelableExtra("product")
        }

        if (product != null) {
            binding.tvName.text = product!!.name
            binding.tvDescription.text = product!!.description
            binding.tvDisponible.text = product!!.status
            binding.tvCategoria.text = product!!.category
            binding.tvTotalPriceQ.text = "Q.${product!!.price.toString()}0"
            Glide.with(binding.imgProduct).load(product!!.imgUrl).into(binding.imgProduct)
            Glide.with(binding.imgBackground).load(product!!.imgUrl).into(binding.imgBackground)

            if (status != product?.status) {
                binding.notIn.visibility = View.VISIBLE
                if (product!!.backOn.isNullOrEmpty()) {
                    binding.tvDisponibleAt.text = "No sabemos cuando volverá a estar disponible."
                    binding.btnAddCart.isEnabled = false
                    binding.btnBuyIt.isEnabled = false
                } else {
                    binding.tvDisponibleAt.text = product?.backOn
                    binding.btnAddCart.isEnabled = true
                    binding.btnBuyIt.isEnabled = true
                }
            } else {
                binding.notIn.visibility = View.GONE
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                binding.imgBackground.setRenderEffect(
                    RenderEffect.createBlurEffect(
                        30f,
                        30f,
                        Shader.TileMode.CLAMP
                    )
                )
            }
        }
    }

    private fun click() {
        auth.currentUser?.uid
        val usrId = auth.currentUser?.uid
        val productos = product
        binding.let {
            it.btnAddCart.setOnClickListener {
                cantidad = binding.etNewQuantity.text.toString().toInt()
                if (auth.currentUser?.uid.isNullOrEmpty()) {
                    Toast.makeText(
                        this,
                        "No puedes agregar al carrito, inicia sesión o crea una cuenta.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (cantidad!! > 0 && product!!.status == status) {
                    val dataP = CardProdModel(
                        userId = usrId,
                        name = productos!!.name.toString(),
                        price = productos.price,
                        quantity = binding.etNewQuantity.text.toString().toInt(),
                        image = productos.imgUrl,
                        total = productos.price.toString()
                            .toDouble() * binding.etNewQuantity.text.toString().toInt()
                    )
                    addToCart(dataP)
                } else {
                    Toast.makeText(
                        this,
                        "La cantidad debe ser mayor a 0. o no hay producto disponible",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            it.btnBuyIt.setOnClickListener {
                cantidad = binding.etNewQuantity.text.toString().toInt()
                if (auth.currentUser?.uid.isNullOrEmpty() && product!!.status == status) {
                    binding.formContainer.visibility = View.VISIBLE
                    binding.btnAddCart.isEnabled = false
                    binding.btnBuyIt.isEnabled = false
                    binding.etNewQuantity.isEnabled = false
                    binding.btnConfirm.setOnClickListener {
                        if (binding.etName.text.isNullOrEmpty() || binding.etLastName.text.isNullOrEmpty() || binding.etAddress.text.isNullOrEmpty()) {
                            Toast.makeText(
                                this,
                                "Todos los campos son obligatorios",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            buyNow()
                        }
                    }
                    binding.btnCancel.setOnClickListener {
                        binding.formContainer.visibility = View.GONE
                        binding.btnAddCart.isEnabled = true
                        binding.btnBuyIt.isEnabled = true
                        binding.etNewQuantity.isEnabled = true
                    }
                } else if (cantidad!! > 0 && product!!.status == status) {
                    buyNow()
                } else {
                    Toast.makeText(
                        this,
                        "La cantidad debe ser mayor a 0. o no hay producto disponible",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }

    private fun addToCart(dataP: CardProdModel) {
        val db = FirebaseFirestore.getInstance()
        val cartRef = db.collection(Constants.PATH_CART).document()
        val documentId = cartRef.id
        cartRef
            .set(dataP.copy(prodId = documentId))
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val snackbar =
                        Snackbar.make(
                            binding.root,
                            "Producto agregado al carrito",
                            Snackbar.LENGTH_SHORT
                        )
                    snackbar.show()
                } else {
                    // Error al crear el documento
                    println("Error adding document to Firestore: ${task.exception}")
                    Toast.makeText(
                        this,
                        "No se pudo agregar el producto al carrito",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                // Error al crear el documento
                println("Failed to add document to Firestore: $e")
                Toast.makeText(
                    this,
                    "No se pudo agregar el producto al carrito",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun buyNow() {
        //Fix quantity default 1
        var nuevaCantidad = binding.etNewQuantity.text.toString().toInt()
        if (nuevaCantidad.equals(null)) {
            nuevaCantidad = "1".toInt()
        } else {
            setNuevaCantidad(nuevaCantidad)
        }

        var pedido = ""
        pedido = pedido + "Detalles de mi pedido:" + "\n"
        pedido += "\n"
        pedido += "\n"
        pedido += "Nombre: ${binding.etName.text.toString().trim()}"
        pedido += "\n"
        pedido += "Apellido: ${binding.etLastName.text.toString().trim()}"
        pedido += "\n"
        pedido += "Dirección: ${binding.etAddress.text.toString().trim()}"
        pedido += "\n"
        pedido += "___________________________"

        val total: Double = product?.price.toString().toDouble() * nuevaCantidad
        binding.let {
            pedido = pedido +
                    "\n" +
                    "\n" +
                    "Producto: ${product?.name.toString()}" +
                    "\n" +
                    "Cantidad: $nuevaCantidad" +
                    "\n" +
                    "Precio: Q. ${product?.price.toString()}" +
                    "\n" +
                    "___________________________" +
                    "\n" +
                    "TOTAL: Q.${total}"
        }

        val url = "https://wa.me/50258700004?text=$pedido"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }

    @SuppressLint("SetTextI18n")
    private fun setNuevaCantidad(cantidad: Int) {
        if (binding.etNewQuantity.text.isNullOrEmpty()) {
            binding.etNewQuantity.setText("1").toString().toInt()
        } else {
            binding.etNewQuantity.setText("$cantidad")
            cantidadI = cantidad
        }
    }
}
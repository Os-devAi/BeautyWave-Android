package com.nexusdev.beautywave.view.products

import android.annotation.SuppressLint
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.nexusdev.beautywave.R
import com.nexusdev.beautywave.databinding.ActivityDetailsBinding
import com.nexusdev.beautywave.model.ProductsModel

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding

    private var product: ProductsModel? = null
    private var status: String = "Disponible"
    private var cantidad: Int? = 0
    private var total: Double? = 0.0
    private val auth = FirebaseAuth.getInstance()

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
                    saveToCart()
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

                } else if (cantidad!! > 0 && product!!.status == status) {
                    saveToCart()
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

    private fun saveToCart() {
        TODO("Not yet implemented")
    }


}
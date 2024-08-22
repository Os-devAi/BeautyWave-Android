package com.nexusdev.beautywave

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject
import com.nexusdev.beautywave.adapter.ImagesAdapter
import com.nexusdev.beautywave.adapter.ProductsAdapter
import com.nexusdev.beautywave.databinding.ActivityMainBinding
import com.nexusdev.beautywave.entities.Constants
import com.nexusdev.beautywave.model.ImagesModel
import com.nexusdev.beautywave.model.ProductsModel
import com.nexusdev.beautywave.view.products.DetailsActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ProductsAdapter
    private lateinit var firestoreListener: ListenerRegistration


    private val handler = Handler(Looper.getMainLooper())
    private var index = 0
    private val delay: Long = 150

    private lateinit var typewriterTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)


        typewriterTextView = binding.txtTitle
        val textToDisplay = "BeautyWave"
        typewriterEffect(textToDisplay)

        //Mostrar la galería
        showImagesGallery()
        getAllActiveData()
        setupSearchView()
        clicks()
    }

    override fun onPause() {
        super.onPause()
        firestoreListener.remove()
    }

    override fun onResume() {
        super.onResume()
        getAllActiveData() // Reactivar listener
    }

    private fun setupSearchView() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText.orEmpty())
                return true
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun clicks() {
        binding.let {
            it.btnHideGallery.setOnClickListener {
                if (binding.btnHideGallery.text == "Ocultar Galería") {
                    binding.btnHideGallery.text = "Mostrar Galería"
                    binding.galleryContainer.visibility = View.GONE
                } else {
                    binding.btnHideGallery.text = "Ocultar Galería"
                    binding.galleryContainer.visibility = View.VISIBLE
                }
            }
            it.btnIn.setOnClickListener {
                getAllActiveData()
            }
            it.btnLowPrice.setOnClickListener {
                getAllActiveDataBy()
            }
            it.btnAll.setOnClickListener {
                getAllData()
            }
            it.btnMenu.setOnClickListener {
                Toast.makeText(this, "Función no disponible", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //animation for text like write machine
    @SuppressLint("SetTextI18n")
    private fun typewriterEffect(text: String) {
        if (index < text.length) {
            typewriterTextView.text = typewriterTextView.text.toString() + text[index]
            index++
            handler.postDelayed({ typewriterEffect(text) }, delay)
        }
    }

    private fun showImagesGallery() {
        val recyclerView = binding.recyclerView
        val imagesList = listOf(
            ImagesModel("https://media.istockphoto.com/id/1296705483/es/foto/elabora-productos-basados-en-podios-blancos-sobre-fondo-rosa-en-pastel.jpg?s=612x612&w=0&k=20&c=PBcaQ7AE-Wlm5_l_kKC67MCuORp9oYh7KOuqiCHKnW4="),
            ImagesModel("https://beautycosmetics-eg.com/ecdata/stores/VFUPGN5656/image/data/s01-update2.jpg"),
            ImagesModel("https://images.squarespace-cdn.com/content/v1/5c4f6ba1e2ccd1ee6075495d/1624288712010-08OMGX6V80Z0S5DNTP3C/The+Difference+Between+Pharmaceutical+vs.+Cosmetic+Products"),
            ImagesModel("https://img.freepik.com/premium-photo/beauty-cosmetics-woman-with-brushes-makeup-purple-background-skincare-fashion-style-cosmetology-aesthetic-girl-with-brush-set-foundation-beauty-products-facial_590464-121539.jpg"),
            ImagesModel("https://www.sgs.com/-/media/sgscorp/images/knowledge-solutions/v-label-for-cosmetics-personal-care-and-household-products.cdn.en-AR.1.png"),
        )

        recyclerView.layoutManager =
            StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
        recyclerView.adapter = ImagesAdapter(imagesList)
    }

    @SuppressLint("SetTextI18n")
    private fun getAllActiveData() {
        binding.txtSubMenuTitle.text = "Productos Disponibles"
        val db = FirebaseFirestore.getInstance()

        val productRef = db.collection(Constants.PATH_PRODUCTS)
        firestoreListener =
            productRef.whereEqualTo("status", "Disponible").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(
                        this,
                        "Error al consultar datos, verifica tu conexión a internet.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                val prodList = mutableListOf<ProductsModel>()
                for (snapshot in snapshot!!.documentChanges) {
                    val product = snapshot.document.toObject(ProductsModel::class.java)
                    product.id = snapshot.document.id

                    when (snapshot.type) {
                        DocumentChange.Type.ADDED -> prodList.add(product)
                        DocumentChange.Type.MODIFIED -> {
                            val index = prodList.indexOfFirst { it.id == product.id }
                            if (index != -1) {
                                prodList[index] = product
                                adapter.notifyItemChanged(index)
                            }
                        }

                        DocumentChange.Type.REMOVED -> {
                            val index = prodList.indexOfFirst { it.id == product.id }
                            if (index != -1) {
                                prodList.removeAt(index)
                                adapter.notifyItemRemoved(index)
                            }
                        }
                    }
                }

                configRecyclerView(prodList)
            }
    }

    @SuppressLint("SetTextI18n")
    private fun getAllActiveDataBy() {
        binding.txtSubMenuTitle.text = "Ordenados por Precio"
        val db = FirebaseFirestore.getInstance()

        val productRef = db.collection(Constants.PATH_PRODUCTS)
        firestoreListener = productRef.whereEqualTo("status", "Disponible").orderBy("price")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(
                        this,
                        "Error al consultar datos, verifica tu conexión a internet.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                val prodList = mutableListOf<ProductsModel>()
                for (snapshot in snapshot!!.documentChanges) {
                    val product = snapshot.document.toObject(ProductsModel::class.java)
                    product.id = snapshot.document.id

                    when (snapshot.type) {
                        DocumentChange.Type.ADDED -> prodList.add(product)
                        DocumentChange.Type.MODIFIED -> {
                            val index = prodList.indexOfFirst { it.id == product.id }
                            if (index != -1) {
                                prodList[index] = product
                                adapter.notifyItemChanged(index)
                            }
                        }

                        DocumentChange.Type.REMOVED -> {
                            val index = prodList.indexOfFirst { it.id == product.id }
                            if (index != -1) {
                                prodList.removeAt(index)
                                adapter.notifyItemRemoved(index)
                            }
                        }
                    }
                }

                configRecyclerView(prodList)
            }
    }

    @SuppressLint("SetTextI18n")
    private fun getAllData() {
        binding.txtSubMenuTitle.text = "Todos los Productos"
        val db = FirebaseFirestore.getInstance()

        val productRef = db.collection(Constants.PATH_PRODUCTS)
        firestoreListener = productRef.orderBy("price").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Toast.makeText(
                    this,
                    "Error al consultar datos, verifica tu conexión a internet.",
                    Toast.LENGTH_SHORT
                ).show()
                return@addSnapshotListener
            }

            val prodList = mutableListOf<ProductsModel>()
            for (snapshot in snapshot!!.documentChanges) {
                val product = snapshot.document.toObject(ProductsModel::class.java)
                product.id = snapshot.document.id

                when (snapshot.type) {
                    DocumentChange.Type.ADDED -> prodList.add(product)
                    DocumentChange.Type.MODIFIED -> {
                        val index = prodList.indexOfFirst { it.id == product.id }
                        if (index != -1) {
                            prodList[index] = product
                            adapter.notifyItemChanged(index)
                        }
                    }

                    DocumentChange.Type.REMOVED -> {
                        val index = prodList.indexOfFirst { it.id == product.id }
                        if (index != -1) {
                            prodList.removeAt(index)
                            adapter.notifyItemRemoved(index)
                        }
                    }
                }
            }

            configRecyclerView(prodList)
        }
    }


    private fun configRecyclerView(itemList: List<ProductsModel>) {
        adapter = ProductsAdapter(itemList.toMutableList())
        binding.recyclerViewProducts.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = this@MainActivity.adapter
        }

        adapter.onItemClick = {
            val i = Intent(this@MainActivity, DetailsActivity::class.java)
            i.putExtra("product", it)
            startActivity(i)
        }
    }
}
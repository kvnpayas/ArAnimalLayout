package com.example.aranimallayout.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.aranimallayout.R

class HomeFragment : Fragment() {

    private lateinit var imageViewLeft: ImageView
    private lateinit var imageViewRight: ImageView
    private lateinit var imageViewCenterBottom: ImageView
    private lateinit var rotateAnimation: Animation
    private var currentAnimatingImageView: ImageView? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        imageViewLeft = view.findViewById(R.id.imageViewLeft)
        imageViewRight = view.findViewById(R.id.imageViewRight)
        imageViewCenterBottom = view.findViewById(R.id.imageViewCenterBottom)

        rotateAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate)

        val animationListener = object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // Animation has ended, stop the animation
                currentAnimatingImageView?.clearAnimation()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        }

        rotateAnimation.setAnimationListener(animationListener)

        val clickListener = View.OnClickListener { clickedView ->
            // Stop the animation of the previously animating ImageView
            currentAnimatingImageView?.clearAnimation()

            // Start the animation of the clicked ImageView
            val imageView = clickedView as ImageView
            imageView.startAnimation(rotateAnimation)
            currentAnimatingImageView = imageView

            Handler(Looper.getMainLooper()).postDelayed({
                imageView.clearAnimation()
            }, 3000)
        }

        imageViewLeft.setOnClickListener(clickListener)
        imageViewRight.setOnClickListener(clickListener)
        imageViewCenterBottom.setOnClickListener(clickListener)

        val hoverListener = View.OnHoverListener { hoveredView, event ->
            // Stop the animation of the previously animating ImageView
            currentAnimatingImageView?.clearAnimation()

            // Start the animation of the hovered ImageView
            val imageView = hoveredView as ImageView
            imageView.startAnimation(rotateAnimation)
            currentAnimatingImageView = imageView

            Handler(Looper.getMainLooper()).postDelayed({
                imageView.clearAnimation()
            }, 3000)
            true
        }

        imageViewLeft.setOnHoverListener(hoverListener)
        imageViewRight.setOnHoverListener(hoverListener)
        imageViewCenterBottom.setOnHoverListener(hoverListener)

        return view
    }
}
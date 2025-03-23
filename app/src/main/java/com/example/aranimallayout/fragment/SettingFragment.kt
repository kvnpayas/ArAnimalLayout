import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import com.example.aranimallayout.MainActivity
import com.example.aranimallayout.R

class SettingFragment : Fragment() {

    private lateinit var mainActivity: MainActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            mainActivity = context
        } else {
            throw RuntimeException("$context must be MainActivity")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        val muteButton: ToggleButton = view.findViewById(R.id.muteButton)
        val volumeSeekBar: SeekBar = view.findViewById(R.id.volumeSeekBar)

        muteButton.setOnCheckedChangeListener { _, isChecked ->
            mainActivity.setBackgroundMusicMute(isChecked)
        }

        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mainActivity.backgroundMusicVolume = progress / 100.0f // Use property assignment
                    if (!mainActivity.backgroundMusicMuted){
                        mainActivity.setMediaPlayerVolume(progress / 100.0f)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        volumeSeekBar.progress = (mainActivity.backgroundMusicVolume * 100).toInt()

        return view
    }
}
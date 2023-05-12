package fastcampus.aop.part4.chapter01

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import fastcampus.aop.part4.chapter01.adapter.VideoAdapter
import fastcampus.aop.part4.chapter01.databinding.FragmentPlayerBinding
import fastcampus.aop.part4.chapter01.dto.VideoDto
import fastcampus.aop.part4.chapter01.service.VideoService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.abs

class PlayerFragment : Fragment(R.layout.fragment_player) {


    private var binding: FragmentPlayerBinding? = null
    private lateinit var videoAdapter: VideoAdapter
    private var player: SimpleExoPlayer? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentPlayerBinding = FragmentPlayerBinding.bind(view)
        binding = fragmentPlayerBinding


        initMotionLayoutEvent(fragmentPlayerBinding)
        initRecyclerView(fragmentPlayerBinding)
        initPlayer(fragmentPlayerBinding)
        initControlButton(fragmentPlayerBinding)

        getVideoList()
    }


    // 모션레이아웃을 메인 레이아웃에 연결
    private fun initMotionLayoutEvent(fragmentPlayerBinding: FragmentPlayerBinding) {
        fragmentPlayerBinding.playerMotionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {}

            // 모션이 변경될 때
            override fun onTransitionChange(motionLayout: MotionLayout?, startId: Int, endId: Int, progress: Float) {
                binding?.let {
                    (activity as MainActivity).also { mainActivity ->
                        mainActivity.findViewById<MotionLayout>(R.id.mainMotionLayout).progress = abs(progress) //프래그먼트 모션레이아웃 진행도에 따라서 메인레이아웃 모션레이아웃 진행도 변경

                    }
                }
            }

            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {

            }

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {

            }
        })
    }

    private fun initRecyclerView(fragmentPlayerBinding: FragmentPlayerBinding) {

        // 아이템 클릭하면 play에 전달
        videoAdapter = VideoAdapter(callback = { url, title ->
            play(url, title)
        })

        fragmentPlayerBinding.fragmentRecyclerView.apply {
            adapter = videoAdapter
            layoutManager = LinearLayoutManager(context)
        }

    }

    private fun initPlayer(fragmentPlayerBinding: FragmentPlayerBinding) {

        context?.let {
            player = SimpleExoPlayer.Builder(it).build()
        }

        fragmentPlayerBinding.playerView.player = player

        binding?.let {
            player?.addListener(object: Player.EventListener {

                // 재생버튼 아이콘 변경하는 리스너 , 재생중 상태 변경 되는것을 감지하기 위해서 리스너 등록
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)

                    if (isPlaying) {
                        it.bottomPlayerControlButton.setImageResource(R.drawable.ic_baseline_pause_24)

                    } else {
                        it.bottomPlayerControlButton.setImageResource(R.drawable.ic_baseline_play_arrow_24)

                    }
                }
            })
        }
    }

    private fun initControlButton(fragmentPlayerBinding: FragmentPlayerBinding) {
        fragmentPlayerBinding.bottomPlayerControlButton.setOnClickListener {
            val player = this.player ?: return@setOnClickListener

            // 플레이어가 재생중일때 플레이 정지 , 재생중이 아닐때 플레이
            // 위에 플레이어에 리스너를 달아놔서 재생중이면 (재생아이콘) , 아니면 (일시정지아이콘)으로 변경됨
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }

    }

    // 레트로 핏으로 데이터 받아오고 어뎁터에 리스트 전달
    private fun getVideoList() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://run.mocky.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(VideoService::class.java).also {

            it.listVideos()
                .enqueue(object: Callback<VideoDto> {
                    override fun onResponse(call: Call<VideoDto>, response: Response<VideoDto>) {
                        if (response.isSuccessful.not()) {
                            return
                        }
                        response.body()?.let { videoDto ->
                            videoAdapter.submitList(videoDto.videos)
                        }
                    }

                    override fun onFailure(call: Call<VideoDto>, t: Throwable) {
                        // 예외처리
                    }
                })
        }
    }

    fun play(url: String, title: String) {


        // mp4로 변환해서 prepare()매서드로 초깃값 설정하고 , play() 매서드를 이용해서 재생
        context?.let {
            val dataSourceFactory = DefaultDataSourceFactory(it)
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.parse(url)))
            player?.setMediaSource(mediaSource)
            player?.prepare()
            player?.play()
        }

     // 프래그먼트의 모션레이아웃을 꽉찬화면으로 변경하고 , 타이틀을 받아온 title 값으로 지정
        binding?.let {
            it.playerMotionLayout.transitionToEnd()
            it.bottomTitleTextView.text = title

        }
    }

    // 잠깐 나갔다오거나 , 종료햇을때 플레이어 종료
    override fun onStop() {
        super.onStop()

        player?.pause()
    }


    override fun onDestroy() {
        super.onDestroy()

        binding = null
        player?.release()
    }
}
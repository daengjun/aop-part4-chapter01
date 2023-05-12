package fastcampus.aop.part4.chapter01

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout


// 프래그먼트 영역이 클릭됬을때만 터치이벤트를 가져갈수있도록 커스텀뷰 생성 (모션레이아웃 커스텀)
class CustomMotionLayout(context: Context, attributeSet: AttributeSet? = null) :
    MotionLayout(context, attributeSet) {

    // 이값이 true일때만 터치이벤트를 가질수 있음
    private var motionTouchStarted = false
    private val mainContainerView by lazy {
        findViewById<View>(R.id.mainContainerLayout)
    }
    private val hitRect = Rect()

    init {
        setTransitionListener(object : TransitionListener {
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {}

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {}

            // 모션이벤트가 완료 될때 터치이벤트를 false로 바꿔줌 -> 리사이클러뷰 터치 가능
            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                motionTouchStarted = false
            }

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {}

        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            // 손을떼거나 취소일때는  사용 x
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                motionTouchStarted = false
                return super.onTouchEvent(event)
            }
        }

        if (!motionTouchStarted) {
            // 컨테이너 영역에서 박스에서 터치가 일어났는지 확인
            mainContainerView.getHitRect(hitRect)
            motionTouchStarted = hitRect.contains(event.x.toInt(), event.y.toInt())
        }

        // 제스쳐와 , 터치 결과값 반환
        return super.onTouchEvent(event) && motionTouchStarted
    }

    private val gestureListener by lazy {
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                // 모션(제스쳐)이 메인컨테이너 안에서 일어났는지
                mainContainerView.getHitRect(hitRect)
                return hitRect.contains(e1.x.toInt(), e1.y.toInt())
            }
        }
    }

    // onscroll 부분을 정의한 리스너를 전달해서 GestureDetector 객체 생성
    private val gestureDetector by lazy {
        GestureDetector(context, gestureListener)
    }


    // 아마도 다른뷰에 터치이벤트 양보하는걸텐데.. 검색
    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

}
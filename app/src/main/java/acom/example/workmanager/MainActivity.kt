package acom.example.workmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.work.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            //构建周期性的后台任务 周期间隔不能短于15分钟
            val request = PeriodicWorkRequest.Builder(SimpleWorker::class.java, 15, TimeUnit.MINUTES).build()
            //构建单次运行的后台任务
            val request1 = OneTimeWorkRequest.Builder(SimpleWorker::class.java)
                    //指定的延迟时间后运行
                .setInitialDelay(5, TimeUnit.MINUTES)
                    //给后台任务添加标签 方便后续通过标签来取消后台任务 没有标签就需要通过id一个一个取消
                .addTag("simple")
                //如果doWork中返回的时Result.retry() 可以使用该方法重新执行任务 第一个参数表示下次重新执行的方式 第二三表示在多久后重新执行该任务 不能超过10秒
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(this)
                    //会返回一个LiveData对象 可以对这个对象进行观察
                .getWorkInfoByIdLiveData(request.id)
                .observe(this) {
                    if(it.state == WorkInfo.State.SUCCEEDED) {
                        Log.d("MainActivity", "do work succeeded")
                    }else if(it.state == WorkInfo.State.FAILED) {
                        Log.d("MainActivity", "do work failed")
                    }
                }
            //通过标签取消后台任务  取消该标签标记的所有后台任务
            WorkManager.getInstance(this).cancelAllWorkByTag("simple")
            //通过id来取消
            WorkManager.getInstance(this).cancelWorkById(request.id)
            //取消所有后台任务
            WorkManager.getInstance(this).cancelAllWork()

        }
    }
}
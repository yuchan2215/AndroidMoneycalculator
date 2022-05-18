package xyz.miyayu.android.moneycalculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //金額一覧を作成
        val list = mutableListOf<MutableMap<String, Int>>()
        listOf(1, 5, 10, 50, 100, 500, 1000, 2000, 5000, 10000).forEach {
            list.add(mutableMapOf("money" to it))
        }
        //金額に円を足してテキストとする。
        val from = arrayOf("money")
        val to = intArrayOf(R.id.moneyType)
        val menu = findViewById<ListView>(R.id.lvItems)
        val adapter = SimpleAdapter(this@MainActivity, list, R.layout.money_input, from, to)
        adapter.viewBinder = MoneyViewBinder()
        menu.adapter = adapter

    }

    /**
     * 円を付け足すViewBinder
     */
    private inner class MoneyViewBinder : SimpleAdapter.ViewBinder {
        override fun setViewValue(view: View?, data: Any?, textRepresentation: String?): Boolean {
            //MoneyTypeなら？
            if (view?.id == R.id.moneyType) {
                //円を足す
                val text = "${data}${getString(R.string.yen)}"
                val textView = view as TextView
                textView.text = text
                return true
            }
            return false
        }
    }
}
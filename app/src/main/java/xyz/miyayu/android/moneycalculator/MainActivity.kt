package xyz.miyayu.android.moneycalculator

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.databinding.DataBindingUtil
import xyz.miyayu.android.moneycalculator.databinding.MoneyInputBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //利用するお金の種類
        val moneys = listOf(1, 5, 10, 50, 100, 500, 1000, 2000, 5000, 10000)
        val menu = findViewById<ListView>(R.id.lvItems)
        val adapter = InputAdapter(this@MainActivity, moneys)
        menu.adapter = adapter
    }

    private inner class InputAdapter(context: Context, val items: List<Int>) :
        ArrayAdapter<Int>(context, R.layout.money_input, items) {
        private val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: View
            val binding: MoneyInputBinding
            //新規なら
            if (null == convertView) {
                binding = DataBindingUtil.inflate(inflater, R.layout.money_input, parent, false)
            //再利用されたなら
            } else {
                binding = convertView.tag as MoneyInputBinding
            }
            //値をあてていく
            binding.type = items[position].toString()
            binding.moneyAmount = "0"

            //view等の設定
            view = binding.root
            view.tag = binding
            return view
        }
    }
}
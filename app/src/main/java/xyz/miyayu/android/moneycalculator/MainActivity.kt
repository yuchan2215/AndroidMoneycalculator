package xyz.miyayu.android.moneycalculator

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import xyz.miyayu.android.moneycalculator.databinding.ActivityMainBinding
import xyz.miyayu.android.moneycalculator.databinding.MoneyInputBinding
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    val rootViewModel: AmountsViewModel by lazy {ViewModelProvider(this@MainActivity, ViewModelProvider.AndroidViewModelFactory(application)).get(AmountsViewModel::class.java)
    }
    companion object {
        //使用する金額の種類
        val moneyTypes = listOf(1, 5, 10, 50, 100, 500, 1000, 2000, 5000, 10000)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //main_activityのデータバインディング
        val binding:ActivityMainBinding = DataBindingUtil.setContentView(this@MainActivity,R.layout.activity_main)
        binding.viewModel = rootViewModel
        binding.lifecycleOwner = this

        //データ入力のリスト
        val menu = findViewById<ListView>(R.id.lvItems)

        //Adapterの作成
        val adapter = InputAdapter(this@MainActivity, moneyTypes)
        menu.adapter = adapter
    }

    //入力データのアダプター
    private inner class InputAdapter(context: Context, val items: List<Int>) :
        ArrayAdapter<Int>(context, R.layout.money_input, items) {

        private val inputInflater:LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: View
            val binding: MoneyInputBinding
            //新規なら
            if (null == convertView) {
                //新しくBinderを作成する。
                binding = DataBindingUtil.inflate<MoneyInputBinding?>(inputInflater,R.layout.money_input,parent,false).apply {
                    viewModel = rootViewModel
                    lifecycleOwner = this@MainActivity
                }

                //view等の設定
                view = binding.root
                //TagにはTagObjectを入れておく。
                view.tag = TagObject(-1,null,null,null)
            //再利用されたなら
            } else {
                //TagObjectからbindingを持ってくる。
                binding = (convertView.tag as TagObject).bind as MoneyInputBinding
                //viewはそのまま利用する
                view = convertView
            }
            //TagObjectを取得する
            val tag = view.tag as TagObject
            //Tagの内容のポジションが、現在のポジションと違うなら（再利用されたなら）
            if((tag.position ?: -1) != position){
                //listener,observerを初期化する。
                tag.watcher?.let{
                    binding.inputMoneyAmount.removeTextChangedListener(it)
                }
                tag.observer?.let{
                    rootViewModel.amountMaps.removeObserver(it)
                }
                //ポジションを現在の物にする。
                tag.position = position

                //Observeする
                val observer= AmountMapObserver(tag,binding)
                rootViewModel.amountMaps.observe(this@MainActivity, observer)

                //EditTextのListenerを登録する。
                val watcher = CustomWatcher(view)
                binding.inputMoneyAmount.addTextChangedListener(watcher)

                //Tagの内容を更新。
                tag.watcher = watcher
                tag.observer = observer
            }
            //お金の種類を登録する。
            binding.varMoneyType = items[position].toString()
            tag.bind = binding
            return view
        }
    }
    //BindingのTagの値を管理するdata class
    data class TagObject(var position:Int?,var bind:MoneyInputBinding?,var watcher:CustomWatcher?, var observer: Observer<MutableMap<String,String>>?)
    //ViewModelの値が変更された時に、EditTextの値を変更する
    inner class AmountMapObserver(private val tag:TagObject, private val binding: MoneyInputBinding):Observer<MutableMap<String,String>> {
        override fun onChanged(t: MutableMap<String, String>?) {
            if(t==null)return
            val newValue = t.getOrDefault(tag.position.toString(),0).toString() //更新後の値
            val editTextValue = binding.inputMoneyAmount.text.toString() //更新前の値
            if(newValue != editTextValue){ //もし値が違うのであれば、更新する
                Log.i("ObservedEvent","${tag.position} 番目のデータが更新：　$editTextValue -> $newValue")
                binding.inputMoneyAmount.setText(newValue)
            }
        }
    }
    //EditTextの入力を監視する
    inner class CustomWatcher(private val view: View):TextWatcher{
        private var text = ""
        override fun afterTextChanged(p0: Editable?) {
            //テキストが更新されているなら
            if(p0?.toString() != text) {
                text = p0.toString()
                Log.i("TextWatcherEvent", "${(view.tag as TagObject).position} 番目のデータを更新 : $p0")

                val map = rootViewModel.amountMaps.value //viewModelからmapを取得
                map?.set((view.tag as TagObject).position.toString(), p0.toString()) //マップに値を足す。
                rootViewModel.amountMaps.postValue(map?.toMutableMap()) //オブジェクトを更新する
            }
        }
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    }
    //根幹ViewModel
    class AmountsViewModel: ViewModel(){
        //金額種類と数のマップ
        val amountMaps:MutableLiveData<MutableMap<String,String>> by lazy{
            MutableLiveData<MutableMap<String,String>>().apply {
                this.value = mutableMapOf()
            }
        }
        //金額の合計
        val sumAmount: LiveData<String> = Transformations.map(amountMaps){
            try {
                var sum = 0L
                it.forEach { mapEntry -> //種類*数を足していく。
                    val moneyType = moneyTypes[mapEntry.key.toInt()]
                    val moneyAmount = if (mapEntry.value.isEmpty()) 0L else mapEntry.value.toLong()
                    sum += moneyType * moneyAmount
                }
                return@map "%,d".format(sum)
            }catch(e:Exception){
                return@map "Error"
            }
        }
    }
}
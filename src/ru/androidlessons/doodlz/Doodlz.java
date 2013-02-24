package ru.androidlessons.doodlz;

import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class Doodlz extends Activity {

	private DoodleView doodleView; // создание View
	private SensorManager sensorManager; // отслеживание акселерометра
	private float acceleration; // ускорение
	private float currentAcceleration; // текущее ускорение
	private float lastAcceleration; // последнее ускорение
	private AtomicBoolean dialogIsVisible = new AtomicBoolean();
	// ложь

	// создание идентификаторов для каждого элемента меню
	private static final int COLOR_MENU_ID = Menu.FIRST;
	private static final int WIDTH_MENU_ID = Menu.FIRST + 1;
	private static final int ERASE_MENU_ID = Menu.FIRST + 2;
	private static final int CLEAR_MENU_ID = Menu.FIRST + 3;
	private static final int SAVE_MENU_ID = Menu.FIRST + 4;

	// значение, используемое для идентификации удара устройства
	private static final int ACCELERATION_THRESHOLD = 15000;

	// переменная, которая ссылается на диалоговые окна Choose Color
	// либо Choose Line Width
	private Dialog currentDialog;

	// вызывается после загрузки Activity
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main); // «раздувание» разметки

		// получение ссылок на DoodleView
		doodleView = (DoodleView) findViewById(R.id.doodleView);

		// инициализация значений ускорения
		acceleration = 0.00f;
		currentAcceleration = SensorManager.GRAVITY_EARTH;
		lastAcceleration = SensorManager.GRAVITY_EARTH;

		enableAccelerometerListening(); // прослушивания тряски
	} // конец метода onCreate

	// если приложение находится в фоновом режиме, остановить
	// прослушивание событий сенсора
	@Override
	protected void onPause() {
		super.onPause();
		disableAccelerometerListening(); // не прослушивать тряску
	} //

	// активизация прослушивания событий акселерометра
	private void enableAccelerometerListening() {
		// инициализация SensorManager
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		sensorManager.registerListener(sensorEventListener,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
	} // конец метода enableAccelerometerListening

	// отключение прослушивания событий акселерометра
	private void disableAccelerometerListening() {
		// прекращение прослушивания событий сенсора
		if (sensorManager != null) {
			sensorManager.unregisterListener(sensorEventListener, sensorManager
					.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER));
			sensorManager = null;
		} // конец блока if
	}

	// обработчик событий акселерометра
	private SensorEventListener sensorEventListener = new SensorEventListener() {
		// использование акселерометра для определения
		// встряхивания устройства
		@Override
		public void onSensorChanged(SensorEvent event) {
			// предотвращение отображения других диалоговых окон
			if (!dialogIsVisible.get()) {
				// получение значений x, y и z для SensorEvent
				float x = event.values[0];
				float y = event.values[1];
				float z = event.values[2];

				// сохранение предыдущего значения ускорения
				lastAcceleration = currentAcceleration;

				// вычисление текущего ускорения
				currentAcceleration = x * x + y * y + z * z;

				// вычисление изменения ускорения
				acceleration = currentAcceleration
						* (currentAcceleration - lastAcceleration);

				// если ускорение превышает определенный уровень
				if (acceleration > ACCELERATION_THRESHOLD) {
					// создание нового AlertDialog Builder
					AlertDialog.Builder builder = new AlertDialog.Builder(
							Doodlz.this);

					// создание сообщения AlertDialog
					builder.setMessage(R.string.message_erase);
					builder.setCancelable(true);

					// добавление кнопки Erase
					builder.setPositiveButton(R.string.button_erase,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialogIsVisible.set(false);
									doodleView.clear(); // очистка экрана
								} // конец метода onClick
							} // конец анонимного внутреннего класса
					); // завершение вызова setPositiveButton

					// добавление кнопки Cancel
					builder.setNegativeButton(R.string.button_cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialogIsVisible.set(false);
									dialog.cancel(); // скрытие диалогового окна
								} // конец метода onClick
							} // конец анонимного внутреннего класса
					); // завершение вызова setNegativeButton

					dialogIsVisible.set(true); // диалоговое окно,
					// отображаемое на экране
					builder.show(); // отображение диалогового окна
				} // конец блока if
			} // конец блока if
		} // конец метода onSensorChanged

		// пустое "тело" метода интерфейса SensorEventListener
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		} // конец метода onAccuracyChanged
	};

	// отображает параметры конфигурации в меню
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu); // вызов метода суперкласса

		// добавление параметров в меню
		menu.add(Menu.NONE, COLOR_MENU_ID, Menu.NONE, R.string.menuitem_color);
		menu.add(Menu.NONE, WIDTH_MENU_ID, Menu.NONE,
				R.string.menuitem_line_width);
		menu.add(Menu.NONE, ERASE_MENU_ID, Menu.NONE, R.string.menuitem_erase);
		menu.add(Menu.NONE, CLEAR_MENU_ID, Menu.NONE, R.string.menuitem_clear);
		menu.add(Menu.NONE, SAVE_MENU_ID, Menu.NONE,
				R.string.menuitem_save_image);

		return true; // обработано создание параметров меню
	} // завершение метода onCreateOptionsMenu

	// обработка выбранных параметров меню
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// оператор switch, использующий MenuItem id
		switch (item.getItemId()) {
		case COLOR_MENU_ID:
			showColorDialog(); // диалоговое окно выбора цвета
			return true; // результат обработки события меню
		case WIDTH_MENU_ID:
			showLineWidthDialog(); // диалоговое окно выбора
			// толщины линии
			return true; // результат обработки события меню
		case ERASE_MENU_ID:
			doodleView.setDrawingColor(Color.WHITE); // белый цвет линии
			return true; // результат обработки события меню
		case CLEAR_MENU_ID:
			doodleView.clear(); // очистка doodleView
			return true; // результат обработки события меню
		case SAVE_MENU_ID:
			doodleView.saveImage(); // сохранение текущих изображений
			return true; // результат обработки события меню
		} // конец блока switch

		return super.onOptionsItemSelected(item); // вызов метода
		// суперкласса
	}

	private void showColorDialog() {
		// создание диалогового окна и «раздувание» его содержимого
		currentDialog = new Dialog(this);
		currentDialog.setContentView(R.layout.color_dialog);
		currentDialog.setTitle(R.string.title_color_dialog);
		currentDialog.setCancelable(true);

		// получение ползунков SeekBar цвета и настройка
		// их слушателей onChange
		final SeekBar alphaSeekBar = (SeekBar) currentDialog
				.findViewById(R.id.alphaSeekBar);
		final SeekBar redSeekBar = (SeekBar) currentDialog
				.findViewById(R.id.redSeekBar);
		final SeekBar greenSeekBar = (SeekBar) currentDialog
				.findViewById(R.id.greenSeekBar);
		final SeekBar blueSeekBar = (SeekBar) currentDialog
				.findViewById(R.id.blueSeekBar);

		// регистрация слушателей событий SeekBar
		alphaSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);
		redSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);
		greenSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);
		blueSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);

		// использование текущего цвета рисунка для выбора значений SeekBar
		final int color = doodleView.getDrawingColor();
		alphaSeekBar.setProgress(Color.alpha(color));
		redSeekBar.setProgress(Color.red(color));
		greenSeekBar.setProgress(Color.green(color));
		blueSeekBar.setProgress(Color.blue(color));

		// настройка слушателей кнопки onClickListeneset для класса Color
		Button setColorButton = (Button) currentDialog
				.findViewById(R.id.setColorButton);
		setColorButton.setOnClickListener(setColorButtonListener);

		dialogIsVisible.set(true); // диалоговое окна на экране
		currentDialog.show(); // отображение диалогового окна
	}

	// Интерфейс OnSeekBarChangeListener для ползунков
	// SeekBar в диалоговом окне выбора цвета
	private OnSeekBarChangeListener colorSeekBarChanged = new OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// получение компонентов SeekBar и colorView LinearLayout
			SeekBar alphaSeekBar = (SeekBar) currentDialog
					.findViewById(R.id.alphaSeekBar);
			SeekBar redSeekBar = (SeekBar) currentDialog
					.findViewById(R.id.redSeekBar);
			SeekBar greenSeekBar = (SeekBar) currentDialog
					.findViewById(R.id.greenSeekBar);
			SeekBar blueSeekBar = (SeekBar) currentDialog
					.findViewById(R.id.blueSeekBar);
			View colorView = (View) currentDialog.findViewById(R.id.colorView);

			// отображение текущего цвета
			colorView.setBackgroundColor(Color.argb(alphaSeekBar.getProgress(),
					redSeekBar.getProgress(), greenSeekBar.getProgress(),
					blueSeekBar.getProgress()));
		} // конец метода onProgressChanged

		// требуется указать метод для интерфейса OnSeekBarChangeListener
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		} // конец метода onStartTrackingTouch

		// метод, требуемый для интерфейса OnSeekBarChangeListener
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		} // конец метода onStopTrackingTouch
	}; // конец colorSeekBarChanged

	// Интерфейс OnClickListener, используемый для выбора цвета
	// после выбора кнопки Set Color в диалоговом окне
	private View.OnClickListener setColorButtonListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// получение цвета SeekBar
			SeekBar alphaSeekBar = (SeekBar) currentDialog
					.findViewById(R.id.alphaSeekBar);
			SeekBar redSeekBar = (SeekBar) currentDialog
					.findViewById(R.id.redSeekBar);
			SeekBar greenSeekBar = (SeekBar) currentDialog
					.findViewById(R.id.greenSeekBar);
			SeekBar blueSeekBar = (SeekBar) currentDialog
					.findViewById(R.id.blueSeekBar);

			// выбор цвета линии
			doodleView.setDrawingColor(Color.argb(alphaSeekBar.getProgress(),
					redSeekBar.getProgress(), greenSeekBar.getProgress(),
					blueSeekBar.getProgress()));
			dialogIsVisible.set(false); // диалоговое окно не на экране
			currentDialog.dismiss(); // скрытие диалогового окна
			currentDialog = null; // диалоговое окно не нужно
		} // конец метода onClick
	};

	// отображение диалогового окна, в котором выбирается толщина линии
	private void showLineWidthDialog() {
		// создание диалогового окна и «раздувание» его содержимого
		currentDialog = new Dialog(this);
		currentDialog.setContentView(R.layout.width_dialog);
		currentDialog.setTitle(R.string.title_line_width_dialog);
		currentDialog.setCancelable(true);

		// получение widthSeekBar и его конфигурирование
		SeekBar widthSeekBar = (SeekBar) currentDialog
				.findViewById(R.id.widthSeekBar);
		widthSeekBar.setOnSeekBarChangeListener(widthSeekBarChanged);
		widthSeekBar.setProgress(doodleView.getLineWidth());

		// Настройка onClickListener для кнопки Set Line Width
		Button setLineWidthButton = (Button) currentDialog
				.findViewById(R.id.widthDialogDoneButton);
		setLineWidthButton.setOnClickListener(setLineWidthButtonListener);

		dialogIsVisible.set(true); // диалоговое окно отображается
		// на экране
		currentDialog.show(); // отображение диалогового окна
	}

	// Интерфейс OnSeekBarChangeListener для компонента
	// SeekBar в диалоговом окне width
	private OnSeekBarChangeListener widthSeekBarChanged = new OnSeekBarChangeListener() {
		Bitmap bitmap = Bitmap.createBitmap( // создание Bitmap
				400, 100, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap); // связывание

		// с объектом Canvas

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// получение ImageView
			ImageView widthImageView = (ImageView) currentDialog
					.findViewById(R.id.widthImageView);

			// конфигурирование объекта Paint для текущего значения SeekBar
			Paint p = new Paint();
			p.setColor(doodleView.getDrawingColor());
			p.setStrokeCap(Paint.Cap.ROUND);
			p.setStrokeWidth(progress);

			// очистка растра и перерисовывание линии
			bitmap.eraseColor(Color.WHITE);
			canvas.drawLine(30, 50, 370, 50, p);
			widthImageView.setImageBitmap(bitmap);
		} // конец определения метода onProgressChanged

		// метод, требуемый для интерфейса OnSeekBarChangeListener
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		} // завершение определения метода onStartTrackingTouch

		// метод, требуемый для интерфейса OnSeekBarChangeListener
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		} // конец определения метода onStopTrackingTouch
	}; // конец определения метода widthSeekBarChanged

	// Интерфейс OnClickListener, выполняющий настройку ширины линии
	// после щелчка на кнопке Set Line Width
	private View.OnClickListener setLineWidthButtonListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// получение цвета с помощью SeekBar
			SeekBar widthSeekBar = (SeekBar) currentDialog
					.findViewById(R.id.widthSeekBar);

			// настройка цвета линии
			doodleView.setLineWidth(widthSeekBar.getProgress());
			dialogIsVisible.set(false); // диалоговое окно не на экране
			currentDialog.dismiss(); // скрытие диалогового окна
			currentDialog = null; // диалоговое окно не нужно
		} // конец описания метода onClick
	};// конец описания интерфейса setColorButtonListener

}

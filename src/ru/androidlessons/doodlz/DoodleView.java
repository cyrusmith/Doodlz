package ru.androidlessons.doodlz;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class DoodleView extends View {

	private static final float TOUCH_TOLERANCE = 10;

	private Bitmap bitmap; // область для отображения или сохранения
	private Canvas bitmapCanvas; // рисование на растре
	private Paint paintScreen; // рисование растра на экране
	private Paint paintLine; // рисование линий на растре
	private HashMap<Integer, Path> pathMap; // рисование текущего Paths
	private HashMap<Integer, Point> previousPointMap; // текущие Points

	// Конструктор DoodleView инициализирует DoodleView
	public DoodleView(Context context, AttributeSet attrs) {
		super(context, attrs); // передача содержимого конструктору View

		paintScreen = new Paint(); // применяется для отображения
		// растра на экране

		// настройка начальных настроек отображения
		// для нарисованной линии
		paintLine = new Paint();
		paintLine.setAntiAlias(true); // сглаживание краев
		// нарисованной линии
		paintLine.setColor(Color.BLACK); // по умолчанию выбран черный
		paintLine.setStyle(Paint.Style.STROKE); // сплошная линия
		paintLine.setStrokeWidth(5); // настраивается заданная
		// по умолчанию ширина линии
		paintLine.setStrokeCap(Paint.Cap.ROUND); // скругленные концы
		// линии
		pathMap = new HashMap<Integer, Path>();
		previousPointMap = new HashMap<Integer, Point>();
	} // завершение описания конструктора DoodleView

	// Метод onSizeChanged создает BitMap и Canvas
	// после отображения приложения
	@Override
	public void onSizeChanged(int w, int h, int oldW, int oldH) {
		bitmap = Bitmap.createBitmap(getWidth(), getHeight(),
				Bitmap.Config.ARGB_8888);
		bitmapCanvas = new Canvas(bitmap);
		bitmap.eraseColor(Color.WHITE); // заливка BitMap белым цветом
	}

	public void clear() {
		pathMap.clear(); // удаление всех контуров
		previousPointMap.clear(); // удаление всех предыдущих точек
		bitmap.eraseColor(Color.WHITE); // очистка растра
		invalidate(); // обновление экрана
	} // завершение описания метода clear

	// настройка цвета нарисованной линии
	public void setDrawingColor(int color) {
		paintLine.setColor(color);
	} // завершение описания метода setDrawingColor

	// возврат цвета нарисованной линии
	public int getDrawingColor() {
		return paintLine.getColor();
	} // завершение описания метода getDrawingColor

	// выбор толщины нарисованной линии
	public void setLineWidth(int width) {
		paintLine.setStrokeWidth(width);
	} // завершение описания метода setLineWidth

	// возврат толщины нарисованной линии
	public int getLineWidth() {
		return (int) paintLine.getStrokeWidth();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// рисование фонового экрана
		canvas.drawBitmap(bitmap, 0, 0, paintScreen);

		// для каждого только что нарисованного контура
		for (Integer key : pathMap.keySet())
			canvas.drawPath(pathMap.get(key), paintLine); // рисование линии
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// получение типа события и идентификатора указателя,
		// который вызвал событие
		int action = event.getActionMasked(); // тип события
		int actionIndex = event.getActionIndex(); // указатель (палец)

		// определение типа действия для данного MotionEvent
		// представляет, затем вызывает соответствующий метод обработки
		if (action == MotionEvent.ACTION_DOWN
				|| action == MotionEvent.ACTION_POINTER_DOWN) {
			touchStarted(event.getX(actionIndex), event.getY(actionIndex),
					event.getPointerId(actionIndex));
		} // завершение описания блока if
		else if (action == MotionEvent.ACTION_UP
				|| action == MotionEvent.ACTION_POINTER_UP) {
			touchEnded(event.getPointerId(actionIndex));
		} // конец описания блока else if
		else {
			touchMoved(event);
		} // конец описания блока else

		invalidate(); // перерисовывание
		return true; // использование события касания
	} // конец описания метода onTouchEvent

	// вызывается после завершения пользователем прикосновения
	private void touchEnded(int lineID) {
		Path path = pathMap.get(lineID); // получение соответствующего
		// контура
		bitmapCanvas.drawPath(path, paintLine); // рисует bitmapCanvas
		path.reset(); // переустановка контура
	} // конец описания метода touchEnded

	// вызывается при выполнении перетаскивания в области экрана
	private void touchMoved(MotionEvent event) {
		// для каждого из указателей в данном MotionEvent
		for (int i = 0; i < event.getPointerCount(); i++) {
			// получение идентификатора и индекса указателя
			int pointerID = event.getPointerId(i);
			int pointerIndex = event.findPointerIndex(pointerID);

			// если имеется контур, связанный с указателем
			if (pathMap.containsKey(pointerID)) {
				// получение новых координат указателя
				float newX = event.getX(pointerIndex);
				float newY = event.getY(pointerIndex);

				// получение контура и предыдущей точки, связанных
				// с этим указателем
				Path path = pathMap.get(pointerID);
				Point point = previousPointMap.get(pointerID);

				// вычисление перемещения от точки последнего обновления
				float deltaX = Math.abs(newX - point.x);
				float deltaY = Math.abs(newY - point.y);

				// если расстояние достаточно велико
				if (deltaX >= TOUCH_TOLERANCE || deltaY >= TOUCH_TOLERANCE) {
					// перемещение контура в новое место
					path.quadTo(point.x, point.y, (newX + point.x) / 2,
							(newY + point.y) / 2);

					// хранение новых координат
					point.x = (int) newX;
					point.y = (int) newY;
				} // конец определения блока if
			} // конец блока if
		} // конец блока for
	} // конец определения метода touchMoved

	// вызывается после касания пользователем экрана
	private void touchStarted(float x, float y, int lineID) {
		Path path; // применяется для хранения контура для
		// идентификатора данного прикосновения
		Point point; // применяется для хранения последней точки контура

		// если уже есть контур для идентификатора lineID
		if (pathMap.containsKey(lineID)) {
			path = pathMap.get(lineID); // получение контура
			path.reset(); // переустановка контура из-за нового
			// прикосновения
			point = previousPointMap.get(lineID); // последняя точка контура
		} // конец блока f
		else {
			path = new Path(); // создание нового контура
			pathMap.put(lineID, path); // добавление контура в карту
			point = new Point(); // создание новой точки
			previousPointMap.put(lineID, point); // добавление точки
			// на карту
		} // конец блока else

		// перемещение координат прикосновения
		path.moveTo(x, y);
		point.x = (int) x;
		point.y = (int) y;
	} // конец описания метода touchStarted

	public void saveImage() {
		// воспользуйтесь "Doodlz" с показаниями времени в качестве
		// имени файла изображения
		String fileName = "Doodlz" + System.currentTimeMillis();

		// создание ContentValues и настройка даты нового изображения
		ContentValues values = new ContentValues();
		values.put(Images.Media.TITLE, fileName);
		values.put(Images.Media.DATE_ADDED, System.currentTimeMillis());
		values.put(Images.Media.MIME_TYPE, "image/jpg");

		// получение Uri местоположения, используемого для хранения файлов
		Uri uri = getContext().getContentResolver().insert(
				Images.Media.EXTERNAL_CONTENT_URI, values);

		try {
			// получение OutputStream для uri
			OutputStream outStream = getContext().getContentResolver()
					.openOutputStream(uri);

			// копирование растра в OutputStream
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);

			// очистка и закрытие потока OutputStream
			outStream.flush(); // очистка буфера
			outStream.close(); // закрытие потока

			// отображение сообщения о сохранении изображения
			Toast message = Toast.makeText(getContext(),
					R.string.message_saved, Toast.LENGTH_SHORT);
			message.setGravity(Gravity.CENTER, message.getXOffset() / 2,
					message.getYOffset() / 2);
			message.show(); // отображение сообщения Toast
		} // конец определения блока try
		catch (IOException ex) {
			// отображение сообщения о сохранении изображения
			Toast message = Toast.makeText(getContext(),
					R.string.message_error_saving, Toast.LENGTH_SHORT);
			message.setGravity(Gravity.CENTER, message.getXOffset() / 2,
					message.getYOffset() / 2);
			message.show();
			// отображение сообщения Toast
		} // конец блока catch
	} // конец описания метода saveImage

}

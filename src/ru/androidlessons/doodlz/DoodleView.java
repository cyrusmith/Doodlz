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

	private Bitmap bitmap; // ������� ��� ����������� ��� ����������
	private Canvas bitmapCanvas; // ��������� �� ������
	private Paint paintScreen; // ��������� ������ �� ������
	private Paint paintLine; // ��������� ����� �� ������
	private HashMap<Integer, Path> pathMap; // ��������� �������� Paths
	private HashMap<Integer, Point> previousPointMap; // ������� Points

	// ����������� DoodleView �������������� DoodleView
	public DoodleView(Context context, AttributeSet attrs) {
		super(context, attrs); // �������� ����������� ������������ View

		paintScreen = new Paint(); // ����������� ��� �����������
		// ������ �� ������

		// ��������� ��������� �������� �����������
		// ��� ������������ �����
		paintLine = new Paint();
		paintLine.setAntiAlias(true); // ����������� �����
		// ������������ �����
		paintLine.setColor(Color.BLACK); // �� ��������� ������ ������
		paintLine.setStyle(Paint.Style.STROKE); // �������� �����
		paintLine.setStrokeWidth(5); // ������������� ��������
		// �� ��������� ������ �����
		paintLine.setStrokeCap(Paint.Cap.ROUND); // ����������� �����
		// �����
		pathMap = new HashMap<Integer, Path>();
		previousPointMap = new HashMap<Integer, Point>();
	} // ���������� �������� ������������ DoodleView

	// ����� onSizeChanged ������� BitMap � Canvas
	// ����� ����������� ����������
	@Override
	public void onSizeChanged(int w, int h, int oldW, int oldH) {
		bitmap = Bitmap.createBitmap(getWidth(), getHeight(),
				Bitmap.Config.ARGB_8888);
		bitmapCanvas = new Canvas(bitmap);
		bitmap.eraseColor(Color.WHITE); // ������� BitMap ����� ������
	}

	public void clear() {
		pathMap.clear(); // �������� ���� ��������
		previousPointMap.clear(); // �������� ���� ���������� �����
		bitmap.eraseColor(Color.WHITE); // ������� ������
		invalidate(); // ���������� ������
	} // ���������� �������� ������ clear

	// ��������� ����� ������������ �����
	public void setDrawingColor(int color) {
		paintLine.setColor(color);
	} // ���������� �������� ������ setDrawingColor

	// ������� ����� ������������ �����
	public int getDrawingColor() {
		return paintLine.getColor();
	} // ���������� �������� ������ getDrawingColor

	// ����� ������� ������������ �����
	public void setLineWidth(int width) {
		paintLine.setStrokeWidth(width);
	} // ���������� �������� ������ setLineWidth

	// ������� ������� ������������ �����
	public int getLineWidth() {
		return (int) paintLine.getStrokeWidth();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// ��������� �������� ������
		canvas.drawBitmap(bitmap, 0, 0, paintScreen);

		// ��� ������� ������ ��� ������������� �������
		for (Integer key : pathMap.keySet())
			canvas.drawPath(pathMap.get(key), paintLine); // ��������� �����
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// ��������� ���� ������� � �������������� ���������,
		// ������� ������ �������
		int action = event.getActionMasked(); // ��� �������
		int actionIndex = event.getActionIndex(); // ��������� (�����)

		// ����������� ���� �������� ��� ������� MotionEvent
		// ������������, ����� �������� ��������������� ����� ���������
		if (action == MotionEvent.ACTION_DOWN
				|| action == MotionEvent.ACTION_POINTER_DOWN) {
			touchStarted(event.getX(actionIndex), event.getY(actionIndex),
					event.getPointerId(actionIndex));
		} // ���������� �������� ����� if
		else if (action == MotionEvent.ACTION_UP
				|| action == MotionEvent.ACTION_POINTER_UP) {
			touchEnded(event.getPointerId(actionIndex));
		} // ����� �������� ����� else if
		else {
			touchMoved(event);
		} // ����� �������� ����� else

		invalidate(); // ���������������
		return true; // ������������� ������� �������
	} // ����� �������� ������ onTouchEvent

	// ���������� ����� ���������� ������������� �������������
	private void touchEnded(int lineID) {
		Path path = pathMap.get(lineID); // ��������� ����������������
		// �������
		bitmapCanvas.drawPath(path, paintLine); // ������ bitmapCanvas
		path.reset(); // ������������� �������
	} // ����� �������� ������ touchEnded

	// ���������� ��� ���������� �������������� � ������� ������
	private void touchMoved(MotionEvent event) {
		// ��� ������� �� ���������� � ������ MotionEvent
		for (int i = 0; i < event.getPointerCount(); i++) {
			// ��������� �������������� � ������� ���������
			int pointerID = event.getPointerId(i);
			int pointerIndex = event.findPointerIndex(pointerID);

			// ���� ������� ������, ��������� � ����������
			if (pathMap.containsKey(pointerID)) {
				// ��������� ����� ��������� ���������
				float newX = event.getX(pointerIndex);
				float newY = event.getY(pointerIndex);

				// ��������� ������� � ���������� �����, ���������
				// � ���� ����������
				Path path = pathMap.get(pointerID);
				Point point = previousPointMap.get(pointerID);

				// ���������� ����������� �� ����� ���������� ����������
				float deltaX = Math.abs(newX - point.x);
				float deltaY = Math.abs(newY - point.y);

				// ���� ���������� ���������� ������
				if (deltaX >= TOUCH_TOLERANCE || deltaY >= TOUCH_TOLERANCE) {
					// ����������� ������� � ����� �����
					path.quadTo(point.x, point.y, (newX + point.x) / 2,
							(newY + point.y) / 2);

					// �������� ����� ���������
					point.x = (int) newX;
					point.y = (int) newY;
				} // ����� ����������� ����� if
			} // ����� ����� if
		} // ����� ����� for
	} // ����� ����������� ������ touchMoved

	// ���������� ����� ������� ������������� ������
	private void touchStarted(float x, float y, int lineID) {
		Path path; // ����������� ��� �������� ������� ���
		// �������������� ������� �������������
		Point point; // ����������� ��� �������� ��������� ����� �������

		// ���� ��� ���� ������ ��� �������������� lineID
		if (pathMap.containsKey(lineID)) {
			path = pathMap.get(lineID); // ��������� �������
			path.reset(); // ������������� ������� ��-�� ������
			// �������������
			point = previousPointMap.get(lineID); // ��������� ����� �������
		} // ����� ����� f
		else {
			path = new Path(); // �������� ������ �������
			pathMap.put(lineID, path); // ���������� ������� � �����
			point = new Point(); // �������� ����� �����
			previousPointMap.put(lineID, point); // ���������� �����
			// �� �����
		} // ����� ����� else

		// ����������� ��������� �������������
		path.moveTo(x, y);
		point.x = (int) x;
		point.y = (int) y;
	} // ����� �������� ������ touchStarted

	public void saveImage() {
		// �������������� "Doodlz" � ����������� ������� � ��������
		// ����� ����� �����������
		String fileName = "Doodlz" + System.currentTimeMillis();

		// �������� ContentValues � ��������� ���� ������ �����������
		ContentValues values = new ContentValues();
		values.put(Images.Media.TITLE, fileName);
		values.put(Images.Media.DATE_ADDED, System.currentTimeMillis());
		values.put(Images.Media.MIME_TYPE, "image/jpg");

		// ��������� Uri ��������������, ������������� ��� �������� ������
		Uri uri = getContext().getContentResolver().insert(
				Images.Media.EXTERNAL_CONTENT_URI, values);

		try {
			// ��������� OutputStream ��� uri
			OutputStream outStream = getContext().getContentResolver()
					.openOutputStream(uri);

			// ����������� ������ � OutputStream
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);

			// ������� � �������� ������ OutputStream
			outStream.flush(); // ������� ������
			outStream.close(); // �������� ������

			// ����������� ��������� � ���������� �����������
			Toast message = Toast.makeText(getContext(),
					R.string.message_saved, Toast.LENGTH_SHORT);
			message.setGravity(Gravity.CENTER, message.getXOffset() / 2,
					message.getYOffset() / 2);
			message.show(); // ����������� ��������� Toast
		} // ����� ����������� ����� try
		catch (IOException ex) {
			// ����������� ��������� � ���������� �����������
			Toast message = Toast.makeText(getContext(),
					R.string.message_error_saving, Toast.LENGTH_SHORT);
			message.setGravity(Gravity.CENTER, message.getXOffset() / 2,
					message.getYOffset() / 2);
			message.show();
			// ����������� ��������� Toast
		} // ����� ����� catch
	} // ����� �������� ������ saveImage

}

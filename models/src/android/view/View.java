import edu.stanford.stamp.annotation.Inline;

class View
{
	@Inline
	public  View(android.content.Context context)
	{
		onFinishInflate();

		onMeasure(0,0);
		onLayout(false, 0, 0, 0, 0);
		onSizeChanged(0, 0, 0, 0);

		onDraw(null);

		onTouchEvent(null);
		onTrackballEvent(null);
		onKeyUp(0, null);
		onKeyDown(0, null);

		onFocusChanged(false, 0, null);
		onWindowFocusChanged(false);

		onAttachedToWindow();
		onDetachedFromWindow();
		onWindowVisibilityChanged(0);
	}

	@Inline
    public  void setOnFocusChangeListener(final android.view.View.OnFocusChangeListener l)
	{
		l.onFocusChange(View.this, false);
	}

    // Callback classes and callback setter methods
	@Inline
    public  void setOnClickListener(final android.view.View.OnClickListener l)
    {
		l.onClick(View.this);
    }

	@Inline
    public  void setOnLongClickListener(final android.view.View.OnLongClickListener l)
    {
		l.onLongClick(View.this);
    }

	@Inline
    public  void setOnCreateContextMenuListener(final android.view.View.OnCreateContextMenuListener l)
	{
		l.onCreateContextMenu(null, View.this, null);
	}

	@Inline
    public  void setOnKeyListener(final android.view.View.OnKeyListener l)
	{
		l.onKey(View.this, 0, null);
	}

	@Inline
    public  void setOnTouchListener(final android.view.View.OnTouchListener l)
	{
		l.onTouch(View.this, null);
	}
}

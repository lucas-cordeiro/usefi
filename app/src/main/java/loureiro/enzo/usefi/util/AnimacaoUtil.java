package loureiro.enzo.usefi.util;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AnimacaoUtil {

    public static void alterarTamanhoObjetoHeigth(final View view, Context context, long tempo, float tamanho){

        view.setVisibility(View.VISIBLE);

        ValueAnimator anim = ValueAnimator.ofInt(view.getMeasuredHeight(), convertDiptoPix(context, tamanho));
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = val;
                view.setLayoutParams(layoutParams);

            }
        });
        anim.setDuration(tempo);
        anim.start();
    }

    public static void encolherTamanhoObjetoHeigth(final View view, float tempo){

        ValueAnimator anim = ValueAnimator.ofInt(view.getMeasuredHeight(), 0);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = val;
                view.setLayoutParams(layoutParams);

                if(valueAnimator.getAnimatedFraction() == 1.0f)
                    view.setVisibility(View.GONE);

            }
        });
        anim.setDuration((long) tempo);
        anim.start();
    }

    public static void piscarTextView(final TextView view, int count, long tempo, final int colorInicial, final int colorAnimacao){
        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(tempo);
        animation.setRepeatCount(count);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setTextColor(colorAnimacao);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setTextColor(colorInicial);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(animation);
    }

    public static void piscarTextView(final TextView view, int count, long tempo, final int colorAnimacao){
        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(tempo);
        animation.setRepeatCount(count);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setTextColor(colorAnimacao);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(animation);
    }

    public static AlphaAnimation desaparecerView(long tempo){
        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(tempo);
        animation.setFillAfter(true);
        return animation;
    }

    public static AlphaAnimation mostrarView(long tempo){
        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(tempo);
        animation.setFillAfter(true);
        return animation;
    }

    public static void progressBar(ProgressBar progressBar, View view, boolean ativa){
          if(ativa){
              if(progressBar!=null && progressBar.getVisibility()!=View.VISIBLE) {
                  progressBar.setVisibility(View.VISIBLE);
              }
              if(view!=null) {
                  AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
                  animation.setDuration(1000);
                  animation.setFillAfter(true);
                  view.startAnimation(animation);
                  view.setEnabled(false);
              }
          }
          else{
              if(progressBar!=null && progressBar.getVisibility()!=View.GONE) {
                  progressBar.setVisibility(View.GONE);
              }
              if(view!=null && view.getAlpha()!=0.0f){
                  AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
                  animation.setDuration(1000);
                  animation.setFillAfter(true);
                  view.startAnimation(animation);
                  view.setEnabled(true);
              }
          }
    }

    public static void progressBar(ProgressBar progressBar, View[] view, boolean ativa){
        if(progressBar!=null){
            if(ativa){
                progressBar.setVisibility(View.VISIBLE);
                AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
                animation.setDuration(1000);
                animation.setFillAfter(true);
                for (View item : view){
                    item.startAnimation(animation);
                    item.setEnabled(false);
                }
            }
            else{
                progressBar.setVisibility(View.GONE);
                AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
                animation.setDuration(1000);
                animation.setFillAfter(true);
                for (View item : view){
                    item.startAnimation(animation);
                    item.setEnabled(true);
                }
            }
        }
    }

    public static float getDensity(Context context){
        return context.getResources().getDisplayMetrics().density;
    }

    public static int convertDiptoPix(Context context, float dip){
        float scale = getDensity(context);
        return (int) (dip * scale + 0.5f);
    }

    public static int convertPixtoDip(Context context, int pixel){
        float scale = getDensity(context);
        return (int)((pixel - 0.5f)/scale);
    }

}

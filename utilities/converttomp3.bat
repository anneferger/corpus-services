for /R %%f in (*.wav) do (
    ffmpeg -i %%~pf%%~nf.wav -ab 192k %%~pf%%~nf.mp3  
)
PAUSE
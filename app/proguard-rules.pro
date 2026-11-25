# Regole R8 per il build release di Fretboard Studio.
# Conserva le annotazioni usate da Compose e kotlinx.serialization.
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature

# Navigation3 serializza le chiavi di navigazione: manteniamo la classe della destinazione.
-keep class com.dedio.fretboardstudio.Main { *; }

# Le classi helper di kotlinx.serialization non devono generare warning bloccanti.
-dontwarn kotlinx.serialization.**

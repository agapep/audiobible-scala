audiobible-scala
================

Projekt jest próbą przepisania aplikacji Audiobible. Powstała ona na system android w języku java w listopadzie/październiku 2013. Posiadała trochę testów, ale ich jakość pozostawia wiele do życzenia.

Obecnie w tym repozytorium znajduje się surowa logika aplikacji. W ramach dalszego rozwoju będą się pojawiać cechy specyficzne dla systemu operacyjnego android. Chciałem po prostu zacząć od w miarę świeżego i niezaśmieconego repo.

budowanie aplikacji
=================
Aplikacja składa się obecnie z dwóch projektów sbt: 
- główny (katalog Audiobible/src)
- model (katalog Audiobible/modelProj)

każdy z nich posiada własne testy i niezależny kod. Kod projektu model ma z założenia być niezależy od platformy Android dlatego jego uruchomienie/testowanie może odbywać się na komputerze. W tym celu należy wykonać jedną z poniższych instrukcji:

		modelProj/run
		modelProj/test
		
aby zbudować główną aplikacje używamy:
		
		android:run
		android:test ?

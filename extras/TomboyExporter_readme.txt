Tomboy Exporter is a utility provided as-is by another user.  He was kind enough
to provide it.  Below is how he used it.



chris@chris-ThinkPad-X120e:~/TomboyExporter/bin$ mkdir ../Nixnote
chris@chris-ThinkPad-X120e:~/TomboyExporter/bin$ java Exporter
../Nixnote/ ~/.local/share/tomboy/*.note
chris@chris-ThinkPad-X120e:~/TomboyExporter/bin$ ls ../Nixnote/
personal--ongoing.nnex  stocks--ongoing.nnex  travel.nnex
work--log.nnex
...


At which point I create the notebooks in Nixnote, and import the
individual .nnex's.


As for building, I was able to rebuild it with a simple 'javac
Exporter.java', although that may not be the desired way (there's a
reason I didn't program this myself).

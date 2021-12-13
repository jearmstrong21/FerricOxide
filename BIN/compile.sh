#java -jar ../build/libs/FerricOxide-1.0-SNAPSHOT.jar -i main.fo -o build/main.x86 -r build/main_riscv.txt -x build/main_x86.txt
echo "FO..."
cd ..
./gradlew run --args="-I BIN/include -I BIN/src -i $1.fo -o BIN/build/main.x86"

echo "C..."

cd BIN
clang main.c glad.c build/main.x86 -o exe/$1 -lglfw -lglew -framework OpenGL -framework Cocoa -framework IOKit -framework CoreVideo

echo "Compiled!"
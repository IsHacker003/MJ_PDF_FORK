sed 's/\\n/\n/g' $1 | sed 's/\\t/\t\t\t\t /g' | sed 's/Caused/\t\t\tCaused/g' > fmt.txt

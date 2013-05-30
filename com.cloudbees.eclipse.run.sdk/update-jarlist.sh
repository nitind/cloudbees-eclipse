find cloudbees-sdk -name "*.jar" | sed 's/$/,/' | sed 's/^/ /' > jarlist.txt

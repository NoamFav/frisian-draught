#!/bin/bash

# Start a new tmux session named 'javafx'
tmux new-session -d -s javafx

# Split the window vertically into two panes
tmux split-window -v

# Run 'mvn javafx:run' in both panes
tmux send-keys -t javafx:0.0 'mvn javafx:run' C-m
tmux send-keys -t javafx:0.1 'mvn javafx:run' C-m

# Attach to the tmux session
tmux attach-session -t javafx

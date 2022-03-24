# Step 0: Install and import the libraries 📚
# pip install numpy
# pip install gym
import numpy as np
import gym
import random
import json

# Step 1: Create the environment 🕹️
env = gym.make("Taxi-v3")

# Step 2: Create the Q-table and initialize it 🗄️
state_space = env.observation_space.n
action_space = env.action_space.n
print("There are ", state_space, " possible states and ",
      action_space, " possible actions")

# Create our Q table with state_size rows and action_size columns (500x6)
Q = np.zeros((state_space, action_space))

# Step 3: Define the hyperparameters ⚙️
total_episodes = 25000        # Total number of training episodes
total_test_episodes = 100     # Total number of test episodes
max_steps = 200               # Max steps per episode

learning_rate = 0.01          # Learning rate
gamma = 0.99                  # Discounting rate

# Exploration parameters
epsilon = 1.0                 # Exploration rate
max_epsilon = 1.0             # Exploration probability at start
min_epsilon = 0.001           # Minimum exploration probability
decay_rate = 0.01             # Exponential decay rate for exploration prob


# Step 4: Define the epsilon-greedy policy 🤖
def epsilon_greedy_policy(Q, state):
    # if random number > greater than epsilon --> exploitation
    if(random.uniform(0, 1) > epsilon):
        action = np.argmax(Q[state])
    # else --> exploration
    else:
        action = env.action_space.sample()
    return action

def reduce_epsilon(episode):
    epsilon = min_epsilon + (max_epsilon - min_epsilon) * \
        np.exp(-decay_rate*episode)
    return epsilon


# Step 5: Define the Q-Learning algorithm and train our agent 🧠
def trainAIAgent():
    training_frames = []
    for episode in range(total_episodes):
        # Reset the environment
        state = env.reset()

        step = 0
        done = False

        # Reduce epsilon (because we need less and less exploration)
        epsilon = reduce_epsilon(episode)

        # log render result
        training_frames.append(["Epsode %d !" % (episode)])

        for step in range(max_steps):
            # log render result
            training_frames[episode].append(env.render(mode="ansi"))

            action = epsilon_greedy_policy(Q, state)

            # Take the action (a) and observe the outcome state(s') and reward (r)
            new_state, reward, done, info = env.step(action)

            # Update Q(s,a):= Q(s,a) + lr [R(s,a) + gamma * max Q(s',a') - Q(s,a)]
            Q[state][action] = Q[state][action] + learning_rate * (reward + gamma *
                                                                np.max(Q[new_state]) - Q[state][action])

            # If done : finish episode
            if done == True:
                break

            # Our new state is state
            state = new_state

    with open("./training_frames.json", 'w', encoding="utf-8") as f:
        f.write(json.dumps(training_frames, indent=2,
                sort_keys=True, ensure_ascii=False))

trainAIAgent()
# =========================== Training Finished. ===========================

# Step 6: Let's watch our autonomous 🚖
def testAIAgent():
    rewards = []
    frames = []
    for episode in range(total_test_episodes):
        state = env.reset()
        step = 0
        done = False
        total_rewards = 0
        print("****************************************************")
        print("EPISODE ", episode)
        for step in range(max_steps):
            env.render()
            # Take the action (index) that have the maximum expected future reward given that state
            action = np.argmax(Q[state][:])
            new_state, reward, done, info = env.step(action)
            total_rewards += reward

            if done:
                rewards.append(total_rewards)
                #print ("Score", total_rewards)
                break
            state = new_state
    env.close()
    print("Score over time: " + str(sum(rewards)/total_test_episodes))

testAIAgent()
##
dnf install python3
pip3 install --upgrade pip
pip3 install --user  tensorflow -i https://pypi.tuna.tsinghua.edu.cn/simple


## RNN 读《伊索寓言》 来预测下一个词汇

用示例短文（出自《伊索寓言》）训练一个RNN来预测下一个单词（就像输入法里常见的“联想”功能。

1. 这个是从 github ( https://github.com/roatienza/Deep-Learning-Experiments/tree/master/Experiments/Tensorflow/RNN ) 的地方拉下来的代码，使用lstm写得一个预测下一个单词的序列网络。

2. 参考的教程是 ：https://jizhi.im/blog/post/1hour_lstm  

3. 结构：

    rnn_word.py 是别的写得代码 

    belling_the_cat.txt 是训练集

    text1.py 是我读原来的代码的时候，把一部分代码copy出来，这样可能会更好的理解原来的代码

    text2.py 是另外一个部分的核心代码

    rnn_demo.py 是我根据上述的代码自己写得，是一个rnn的网络模型。这个是核心代码。建议先看这个。

    rnn_demo2.py 是完整的，可以运行的，完整的代码。



### todo1 

这个还没有写完，rnn_demo2.py 的情况可以根据从3个输入的词汇，输出下一个词汇。
那么，如何让它写出一篇文章呢？


```
把预测输出当作下一个字符，再形成新的输入。比如上面的输入时"had a general"，预测的是"council"，然后再把"a general council"当做输入，又预测了下一个单词"to"，以此类推。

最后，LSTM创造了一个还算是人话的故事：

had a general council to consider what measures they could take to outwit their common enemy , the cat . some said this , and some said that but at last a young mouse got


作者：Kaiser
链接：https://zhuanlan.zhihu.com/p/27237560

```

### todo2

因为我们在非常关键的一步做了极大地简化，那就是词汇的编码表征。这里用的是非常低效的独热向量，112个单词的状态空间，就有长度为112的输入/输出向量，而牛津辞典有170000多个单词，即便常用词也有好几万，这个计算量和冗余量会非常之大，引发“维数灾难”。
并且，用独热码对语言进行编码，丢失了词汇之间的关联信息，更好的方法是Word2Vec


### PS :

参考GitHub ： https://github.com/roatienza/Deep-Learning-Experiments/tree/master/Experiments/Tensorflow/RNN

参考文章 ： https://jizhi.im/blog/post/1hour_lstm
























# Generate c code

I used in this project a reccurent neural network to generate c code based on a dataset of c files from the <a href="https://github.com/torvalds/linux">linux repository</a>.

More Information about the project could be find on my medium article : <a href="https://medium.com/@thibo73800/how-to-train-a-neural-network-to-code-by-itself-a432e8a120df"> Here </a>

<a href="https://medium.com/@thibo73800/how-to-train-a-neural-network-to-code-by-itself-a432e8a120df" ><img src="img/leo_vallet.jpeg" /></a>
<center><a href="https://www.linkedin.com/in/leovallet/">Illustration by Léo Vallet</a></center>

## Requirements

<ul>
<li>Python 3</li>
<li>Tensorflow</li>
<li>Numpy</li>
<li>Docopt</li>
<li>Matplotlib</li>
</ul>

## How to train the model

 
    $> python train.py

This file will create two new folders. The first one is the logs folder, you can use tensorboard on it, the other one is the checkpoints folder where the progression of the model is saved. A new checkpoint is saved at the end of each epoch.

## How to use the model

Once your model is trained, you can try to generate a new c file.

    $> python use.py -h
    $> python use.py <path-to-you-ckpt-file>

## Example of function automatically created with this project


    /*
     * Wake_update_const in a completity the allocation files
     * @copurate: if the fault the polynomial semaphores.
     */
    static ssize_t
    remove_slot(struct kioctx *ctx, const char *buf * int id, const char *buf, int len, int len)
    {
      int inode = sec_free(sector);
      spin_unlock(&c->erase_completion_lock);

      if (!c->resectors[i].size)
        return -EINVAL;

      mutex_unlock(&ctx->ring_lock);
      return err;
    }
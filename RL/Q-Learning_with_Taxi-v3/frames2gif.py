from PIL import Image, ImageDraw, ImageFont, ImageFilter
import imageio
import json
import re
import os

def drawImage(text='Hello World', filename='geeks.jpg', xy=(170, 200), fontcolor='#ffffff', groundcolor='#000000'):
    image = Image.new('RGB', xy, groundcolor)
    ttfont = ImageFont.truetype('cour.ttf', size=25, encoding='unic')
    draw = ImageDraw.Draw(image)
    draw.text((0, 0), text, font=ttfont, fill=fontcolor)
    image.save(filename)

def genImage():
    emptycar_escape = re.compile(r'\x1B\[43m.')
    fullcar_escape = re.compile(r'\x1B\[42m.')
    passenger_escape = re.compile(r'\x1B\[34;1m.') # \u001b[34;1m\u001b[43mG
    dest_escape = re.compile(r'\x1B\[35m.') # \u001b[35m\u001b[43mG

    ansi_escape = re.compile(r'\x1B[@-_][0-?]*[ -/]*[@-~]')

    with open('training_frames.json', 'r') as f:
        o = json.load(f)

    for i in range(len(o)):
        print('episode: %d/%d' % (i,len(o)))
        episode = o[i]
        os.mkdir('./imgs/episode_%05d' % (i))
        for j in range(len(episode)):
            step = o[i][j]
            step = emptycar_escape.sub('0', step)
            step = fullcar_escape.sub('8', step)
            step = passenger_escape.sub('*', step)
            step = dest_escape.sub('#', step)
            step = ansi_escape.sub('', step)
            drawImage(step, './imgs/episode_%05d/%04d.jpg' % (i, j))


def genGif(gif_name):
    path=r'./imgs/episode_00001/'
    image_list=[ path+img for img in  os.listdir(path)]
    frames = []
    for image_name in image_list:
        if image_name.endswith('.jpg'):
            frames.append(imageio.imread(image_name))
    # Save them as frames into a gif
    imageio.mimsave(gif_name, frames, 'GIF', duration = 0.2)
 
    return

# genImage()
genGif('train.gif')
